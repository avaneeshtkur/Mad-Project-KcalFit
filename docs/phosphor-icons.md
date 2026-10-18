# Phosphor Icons in Calfit

Calfit uses official Phosphor SVG artwork converted to native Android vector
drawables. There is no production icon-library dependency or runtime download.
Only three Regular-weight assets are integrated so far; application screens still
use their existing icons.

## Why assets

The [official package list](https://github.com/phosphor-icons/core#our-projects)
lists the Compose Multiplatform port as a **community** project. The repository
`phosphor-icons/android` is a launcher icon-pack application, not a Compose UI
dependency. Using the official SVG assets avoids introducing a community wrapper
or changing Calfit's Kotlin/Compose versions just to access icons.

Android [supports vector drawables in Compose](https://developer.android.com/develop/ui/compose/resources#vector-assets-and-image-resources)
through `painterResource`. This is the selected native integration.

## Imported assets and license

Upstream: <https://github.com/phosphor-icons/core>

Pinned revision: `2b75f3ad12b420c9504ef05df8d2564a28f8500e`.

| Official SVG | Android resource |
| --- | --- |
| `assets/regular/barbell.svg` | `R.drawable.phosphor_barbell_regular` |
| `assets/regular/heart.svg` | `R.drawable.phosphor_heart_regular` |
| `assets/regular/fork-knife.svg` | `R.drawable.phosphor_fork_knife_regular` |

Original SVGs and their SHA-256 hashes are retained in `third-party/phosphor/`.
The MIT license is packaged in the APK at `assets/licenses/phosphor-icons.txt`.
The conversion preserves every SVG path and the 256 × 256 viewport; the default
Android size is 24dp. `currentColor` becomes black path fill; Compose supplies the
actual tint. These three SVGs use only paths with no transforms, gradients,
strokes, masks, or unsupported SVG features.

## Kotlin / Compose usage

```kotlin
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kcalfit.app.R

Icon(
    painter = painterResource(R.drawable.phosphor_barbell_regular),
    contentDescription = "Workout", // Use a localized string for an actual control.
    tint = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.size(24.dp),
)
```

Use `contentDescription = null` for decorative icons beside equivalent visible
text. Interactive icons belong in appropriately sized Android controls such as
`IconButton`; a 24dp glyph is not the control's touch target. Match weight and tint
to the surrounding interface when replacing icons in a later task.

## Preview and verification

Open `app/src/debug/java/com/kcalfit/app/ui/icons/PhosphorIconsPreview.kt` in
Android Studio and select Split or Design to see the light and dark previews.
This debug sample has no navigation route and is never invoked by app screens.

Only testing dependencies were added: Compose `ui-test-junit4` for instrumentation
and `ui-test-manifest` for its empty debug activity, both using the existing BOM.

```bash
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.kcalfit.app.ui.icons.PhosphorIconsRenderTest
```

The five isolated rendering tests use Compose `Icon(painterResource(...))` and
check all three assets at 24dp and 48dp in light/dark themes, plus an explicit
color tint. They assert visible foreground pixels, retained negative space,
dimensions, and accessible descriptions. Each test saves a rendered sample PNG
in the debug app's cache at `phosphor-previews/` for visual inspection. They do not
open the login, nutrition, or other application screens.

## Adding icons later

Download selected SVGs from a pinned upstream revision and use Android Studio's
**New → Vector Asset → Local file (SVG, PSD)** importer. Check for conversion
warnings, preserve the original viewport and geometry, and name resources
`phosphor_<icon_name>_<weight>.xml`. Retain the source and update the manifest.
Preview and render-test each import; do not assume every SVG feature is supported.
Duotone assets need their original opacity preserved, and a single tint cannot
provide independent colors for the two layers.
