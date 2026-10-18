package com.kcalfit.app.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.domain.home.calorieRingProgress

@Composable
fun IntakeRing(consumed: Int, goal: Int, modifier: Modifier = Modifier) {
    // animateFloatAsState starts at the actual value, so entering the screen doesn't replay from zero.
    val progress by animateFloatAsState(calorieRingProgress(consumed,goal), tween(650), label = "Calorie intake")
    val animatedCalories by animateIntAsState(consumed, tween(650), label = "Confirmed calorie count")
    val accent = if(consumed > goal && goal > 0) Color(0xFFDCBCFF) else Color.White
    Box(modifier.size(112.dp * LocalDensity.current.fontScale.coerceIn(1f, 1.8f)).semantics(mergeDescendants = true) {
        contentDescription = "$consumed calories of $goal calorie goal"
        progressBarRangeInfo = ProgressBarRangeInfo(calorieRingProgress(consumed,goal),0f..1f)
    }, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), color = accent,
            trackColor = Color.White.copy(alpha = .16f), strokeWidth = 8.dp)
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(animatedCalories.toString(), color = Color.White, fontSize = if(consumed >= 10000) 18.sp else 23.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("kcal", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium)
        }
    }
}
