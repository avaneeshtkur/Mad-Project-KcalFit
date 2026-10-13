package com.kcalfit.app.ui.nakafit

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

object Naka {
    val Ink = Color(0xFF101014)
    val Violet = Color(0xFF6500F5)
    val Muted = Color(0xFF818AA0)
    val Pale: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val Line: Color @Composable get() = MaterialTheme.colorScheme.outline
    val Lavender = Color(0xFFE0CEFF)
    val Lime = Color(0xFFF1FFD2)
    val Pink = Color(0xFFF9DAE0)
}

@Composable
fun NakaPhoto(@DrawableRes image: Int, description: String?, modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center) {
    Image(painterResource(image), description, modifier, alignment = alignment, contentScale = ContentScale.Crop)
}

@Composable
fun NakaPage(title: String, onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            } else Spacer(Modifier.width(40.dp))
            Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (action != null) action() else Spacer(Modifier.width(40.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
            .padding(bottom = 24.dp).imePadding().animateContentSize(), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
fun NakaButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    dark: Boolean = false, outlined: Boolean = false) {
    if (outlined) OutlinedButton(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled,
        shape = RoundedCornerShape(30.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text(label) }
    else Button(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled,
        shape = RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) Naka.Ink else Naka.Violet,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text(label) }
}

@Composable
fun NakaPhotoCard(title: String, subtitle: String, @DrawableRes image: Int,
    onClick: () -> Unit, modifier: Modifier = Modifier, height: Int = 190) {
    Box(modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)) {
        NakaPhoto(image, title, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .78f)))))
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 24.sp, lineHeight = 28.sp)
            Text(subtitle, color = Color.White.copy(alpha = .84f), fontSize = 13.sp)
        }
    }
}

@Composable
fun NakaSection(title: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 19.sp, color = MaterialTheme.colorScheme.onBackground)
        if (actionLabel != null) TextButton(onClick = onAction) { Text(actionLabel, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) }
    }
}

@Composable
fun NakaRow(title: String, subtitle: String = "", onClick: () -> Unit, leading: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) { leading(); Spacer(Modifier.width(12.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            if (subtitle.isNotBlank()) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Naka.Muted)
    }
}

@Composable
fun NakaStat(value: String, label: String, modifier: Modifier = Modifier, color: Color? = null) {
    Surface(modifier, color = color ?: MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(value, color = if (color != null) Naka.Ink else MaterialTheme.colorScheme.onSurface, fontSize = 22.sp)
            Text(label, color = if (color != null) Naka.Ink.copy(alpha = .7f) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
fun FloatingChatbotShortcut(onClick: () -> Unit, modifier: Modifier = Modifier,
    positionX: Float = 0f, positionY: Float = 1f, onPositionChanged: (Float, Float) -> Unit) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val maxX = with(density) { (maxWidth.toPx() - 48.dp.toPx()).coerceAtLeast(0f) }
        val maxY = with(density) { (maxHeight.toPx() - 48.dp.toPx()).coerceAtLeast(0f) }
        val currentX by androidx.compose.runtime.rememberUpdatedState(positionX)
        val currentY by androidx.compose.runtime.rememberUpdatedState(positionY)
        SmallFloatingActionButton(
            onClick = onClick,
            modifier = Modifier.offset { IntOffset((maxX * currentX.coerceIn(0f, 1f)).roundToInt(),
                (maxY * currentY.coerceIn(0f, 1f)).roundToInt()) }.size(48.dp).pointerInput(maxX, maxY) {
                detectDragGestures { change, amount ->
                    change.consume()
                    onPositionChanged(
                        if (maxX > 0) ((currentX * maxX + amount.x) / maxX).coerceIn(0f, 1f) else 0f,
                        if (maxY > 0) ((currentY * maxY + amount.y) / maxY).coerceIn(0f, 1f) else 0f)
                }
            },
            shape = CircleShape, containerColor = Naka.Violet, contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp, pressedElevation = 8.dp)
        ) { Icon(Icons.Default.ChatBubble, "Open chatbot. Drag to move.", modifier = Modifier.size(22.dp)) }
    }
}
