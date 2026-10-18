package com.kcalfit.app.ui.icons

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kcalfit.app.ui.theme.CalFitTheme
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises painterResource + Material3 Icon on Android, without opening Calfit screens. */
@RunWith(AndroidJUnit4::class)
class PhosphorIconsRenderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun light24() = verifyRendering(dark = false, size = 24)
    @Test fun dark24() = verifyRendering(dark = true, size = 24)
    @Test fun light48() = verifyRendering(dark = false, size = 48)
    @Test fun dark48() = verifyRendering(dark = true, size = 48)
    @Test fun explicitTint24() = verifyRendering(dark = false, size = 24, tint = Color(0xFF006B5A))

    private fun verifyRendering(dark: Boolean, size: Int, tint: Color? = null) {
        var foreground = Color.Unspecified
        var background = Color.Unspecified
        compose.setContent {
            CalFitTheme(darkTheme = dark) {
                foreground = tint ?: MaterialTheme.colorScheme.onSurface
                background = MaterialTheme.colorScheme.surface
                PhosphorIconSamples(iconSize = size.dp, tint = foreground, background = background)
            }
        }
        compose.waitForIdle()
        listOf("Barbell", "Heart", "Fork knife").forEach { name ->
            compose.onNodeWithContentDescription(name).assertIsDisplayed()
            val node = compose.onNodeWithTag("phosphor-$name")
                .assertWidthIsEqualTo(size.dp)
                .assertHeightIsEqualTo(size.dp)
            val pixels = node.captureToImage().toPixelMap()
            var ink = 0
            var clear = 0
            for (y in 0 until pixels.height) {
                for (x in 0 until pixels.width) {
                    val color = pixels[x, y]
                    if (close(color, foreground)) ink++
                    if (close(color, background)) clear++
                }
            }
            val area = pixels.width * pixels.height
            assertTrue("$name must paint visible paths with the requested tint", ink > area * 0.05)
            assertTrue("$name must retain its transparent negative space", clear > area * 0.15)
            assertTrue("$name must not become a solid square", ink < area * 0.8)
        }
        val image = compose.onNodeWithTag("phosphor-samples").captureToImage().asAndroidBitmap()
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "phosphor-previews",
        ).apply { mkdirs() }
        val name = if (tint != null) "tinted-$size" else "${if (dark) "dark" else "light"}-$size"
        File(directory, "$name.png").outputStream().use {
            assertTrue(image.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private fun close(actual: Color, expected: Color): Boolean =
        abs(actual.red - expected.red) < 0.03f &&
            abs(actual.green - expected.green) < 0.03f &&
            abs(actual.blue - expected.blue) < 0.03f &&
            actual.alpha > 0.97f
}
