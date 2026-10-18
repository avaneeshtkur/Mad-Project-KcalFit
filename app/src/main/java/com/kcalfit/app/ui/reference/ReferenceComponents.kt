package com.kcalfit.app.ui.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

/** Exact palette labels from the local colors.png; remaining tokens sampled from screens. */
object ReferenceStyle {
    val Blue = Color(0xFF0066EE)
    val Error = Color(0xFFDB471E)
    val Success = Color(0xFF188977)
    val Warning = Color(0xFFF5A524)
    val Choice = Color(0xFFF6F6F8)
    val Line = Color(0xFFD8D8DC)
    val Muted = Color(0xFF777777)
    val LightBlue = Color(0xFFE2F4FD)
    val Selected = Color(0xFFCAE3FF)
    val Progress = Color(0xFF2CC383)
    val Protein = Color(0xFFFFC45A)
    val Carbs = Color(0xFF51BEBD)
    val Fat = Color(0xFF78469C)
    val RingTrack = Color(0xFFE8E1EF)
    val Steps = Color(0xFFEF3868)
    val MealColors = listOf(Color(0xFF203ED2), Color(0xFF1A334F), Blue, Color(0xFF009CED))
}

@Composable fun KcalfitWordmark(modifier: Modifier = Modifier, color: Color = ReferenceStyle.Blue, size: TextUnit = 30.sp) {
    Text("kcalfit", modifier, color = color, fontSize = size, fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp, maxLines = 1)
}
@Composable fun ReferenceIcon(id: Int, description: String?, modifier: Modifier = Modifier, tint: Color? = null) {
    if (tint == null) Image(painterResource(id), description, modifier.size(24.dp))
    else Icon(painterResource(id), description, modifier.size(24.dp), tint = tint)
}
@Composable fun ReferenceHeader(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    // Reserve the larger action width on both sides so the title stays screen-centered.
    Layout(modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), content = {
        Box {
            if (onBack != null) IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            else Spacer(Modifier.width(48.dp))
        }
        Row(Modifier.widthIn(min = 48.dp), content = actions)
        Text(title, textAlign = TextAlign.Center, fontSize = 17.sp)
    }) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val back = measurables[0].measure(loose)
        val actionsPlaceable = measurables[1].measure(loose)
        val side = maxOf(back.width, actionsPlaceable.width)
        val titlePlaceable = measurables[2].measure(loose.copy(maxWidth = (constraints.maxWidth - side * 2).coerceAtLeast(0)))
        val height = constraints.constrainHeight(maxOf(back.height, actionsPlaceable.height, titlePlaceable.height))
        layout(constraints.maxWidth, height) {
            back.placeRelative(0, (height - back.height) / 2)
            actionsPlaceable.placeRelative(constraints.maxWidth - actionsPlaceable.width, (height - actionsPlaceable.height) / 2)
            titlePlaceable.placeRelative((constraints.maxWidth - titlePlaceable.width) / 2, (height - titlePlaceable.height) / 2)
        }
    }
}
@Composable fun ReferenceProgress(segment: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(7) { Box(Modifier.weight(1f).height(4.dp).background(if(it < segment) ReferenceStyle.Progress else ReferenceStyle.Line)) }
    }
}
@Composable fun ReferenceNext(onBack: () -> Unit, onNext: () -> Unit, label: String = "Next", inverse: Boolean = false, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack, Modifier.size(48.dp).background(if(inverse) Color.Transparent else ReferenceStyle.LightBlue, CircleShape)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = if(inverse) Color.White else ReferenceStyle.Blue)
        }
        Button(onNext, Modifier.weight(1f).heightIn(min = 48.dp), enabled = enabled, shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = if(inverse) Color.White else ReferenceStyle.Blue,
                contentColor = if(inverse) ReferenceStyle.Blue else Color.White)) { Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
    }
}
@Composable fun ReferenceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp, border = BorderStroke(.5.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}
