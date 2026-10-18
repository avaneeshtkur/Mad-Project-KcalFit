package com.kcalfit.app.ui.icons

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcalfit.app.R
import com.kcalfit.app.ui.theme.CalFitTheme

/** Isolated debug sample: never called from application screens or navigation. */
@Composable
internal fun PhosphorIconSamples(
    iconSize: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    background: Color = MaterialTheme.colorScheme.surface,
) {
    val icons = listOf(
        "Barbell" to R.drawable.phosphor_barbell_regular,
        "Heart" to R.drawable.phosphor_heart_regular,
        "Fork knife" to R.drawable.phosphor_fork_knife_regular,
    )
    Row(
        modifier = Modifier.background(background).padding(16.dp).testTag("phosphor-samples"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        icons.forEach { (name, drawable) ->
            Box(Modifier.size(iconSize).background(background).testTag("phosphor-$name")) {
                Icon(
                    painter = painterResource(drawable),
                    contentDescription = name,
                    tint = tint,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

@Preview(name = "Phosphor — light", showBackground = true)
@Preview(name = "Phosphor — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PhosphorIconsPreview() {
    CalFitTheme { PhosphorIconSamples() }
}
