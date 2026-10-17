package com.kcalfit.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import com.kcalfit.app.ui.components.IntakeRing
import com.kcalfit.app.ui.components.rememberLocalGreeting
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.components.CalorieGoalDialog
import com.kcalfit.app.ui.nakafit.*
import com.kcalfit.app.ui.viewmodel.CalFitUiState

@Composable
fun HomeScreen(uiState: CalFitUiState, onAddFoodClick: (MealType?) -> Unit, onDeleteEntry: (FoodEntry) -> Unit,
    onUpdateGoal: (Int) -> Unit, darkMode: Boolean, onDarkModeChange: (Boolean) -> Unit,
    onOpenExercise: () -> Unit, onOpenBreakfast: () -> Unit = {}, onOpenGoals: () -> Unit = {},
    onOpenProfile: () -> Unit = {}, onOpenTrack: () -> Unit = {}, onOpenNotifications: () -> Unit = {},
    onWorkout: (String) -> Unit = {}, modifier: Modifier = Modifier,
    onMeal: (MealType) -> Unit = {}, onFood: (FoodEntry) -> Unit = {}) {
    var editGoal by remember { mutableStateOf(false) }
    if (editGoal) CalorieGoalDialog(uiState.dailyGoal, { editGoal = false }, { onUpdateGoal(it); editGoal = false })
    val calorieGoal = uiState.dailyGoal.coerceAtLeast(1)
    val calorieTotal = uiState.totalCalories.coerceAtLeast(0)
    val greeting = rememberLocalGreeting()
    LazyColumn(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(48.dp).clickable(onClick = onOpenProfile), shape = CircleShape, color = Naka.Lavender) {
                    Box(contentAlignment = Alignment.Center) { Text(uiState.userProfile?.name?.take(1)?.uppercase() ?: "N",
                        color = Naka.Violet, fontSize = 18.sp) }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(greeting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    uiState.userProfile?.name?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) }
                }
                IconButton(onOpenNotifications, Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                    Icon(Icons.Default.NotificationsNone, "Reminders", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton({ onDarkModeChange(!darkMode) }) {
                    Icon(if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        if (darkMode) "Switch to light mode" else "Switch to dark mode", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        item {
            Surface(Modifier.fillMaxWidth().clickable(onClick = onOpenBreakfast), color = Naka.Ink, shape = RoundedCornerShape(24.dp)) {
                BoxWithConstraints(Modifier.padding(20.dp)) {
                    val compact = maxWidth < 290.dp || LocalDensity.current.fontScale > 1.3f
                    @Composable fun Labels() {
                        Text("Calorie Intake", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Goal: $calorieGoal kcal", color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(if(calorieTotal > calorieGoal) "${calorieTotal - calorieGoal} kcal above goal"
                            else "${calorieGoal - calorieTotal} kcal remaining", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall)
                        Text(uiState.selectedDate, color = Color.White.copy(alpha = .65f), style = MaterialTheme.typography.labelSmall)
                    }
                    if(compact) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) { IntakeRing(calorieTotal,calorieGoal); Labels() }
                    else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(Modifier.weight(1f)) { Labels() }
                        IntakeRing(calorieTotal,calorieGoal)
                    }
                }
            }
        }
        item {
            val distanceToday = uiState.exerciseEntries.sumOf { it.distanceKm.takeIf(Double::isFinite)?.coerceAtLeast(0.0) ?: 0.0 }
            val durationToday = uiState.exerciseEntries.sumOf { it.durationMinutes.coerceAtLeast(0) }
            NakaSection("Today’s activity", "See Details", onOpenTrack)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeMetric("Calories burnt", "${uiState.caloriesBurned}", "kcal", Icons.Default.LocalFireDepartment, Modifier.weight(1f), onOpenTrack)
                HomeMetric("Distance", if (distanceToday > 0) String.format(java.util.Locale.getDefault(), "%.2f", distanceToday) else "—", "km", Icons.AutoMirrored.Filled.DirectionsRun, Modifier.weight(1f), onOpenTrack)
            }
            Surface(Modifier.fillMaxWidth().clickable(onClick = onOpenTrack), color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Workout time", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    Text("$durationToday min", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        item {
            NakaSection("Nutrition", "Open diary", onOpenBreakfast)
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    MacroProgressRow("Protein", uiState.totalProteinGrams, uiState.targetProteinGrams, MaterialTheme.colorScheme.primary)
                    MacroProgressRow("Carbs", uiState.totalCarbsGrams, uiState.targetCarbsGrams, MaterialTheme.colorScheme.tertiary)
                    MacroProgressRow("Fat", uiState.totalFatGrams, uiState.targetFatGrams, MaterialTheme.colorScheme.secondary)
                }
            }
        }
        item {
            NakaSection("Popular Exercise", "See Details", onOpenExercise)
            Spacer(Modifier.height(8.dp))
            HomeExercise("Squat Low,\nBuild Strength", "24 Exercise", Naka.Lavender, R.drawable.nakafit_strength, { onWorkout("strength") })
            Spacer(Modifier.height(16.dp))
            HomeExercise("Lift Weights,\nGet Stronger", "20 Exercise", Naka.Lime, R.drawable.nakafit_gym_sunset, { onWorkout("weights") })
            Spacer(Modifier.height(16.dp))
            HomeExercise("Morning Run", "30 minutes", Naka.Pink, R.drawable.nakafit_runner, { onWorkout("run") })
        }
        item {
            NakaSection("Daily Goals", "Add Goals", onOpenGoals)
            NakaRow("Daily calorie target", "${uiState.totalCalories} / ${uiState.dailyGoal} kcal", { editGoal = true })
            MealDiarySection(uiState, onMeal, { onAddFoodClick(it) }, onFood, onDeleteEntry)
        }
    }
}

@Composable
private fun MacroProgressRow(label: String, value: Double, target: Int, tint: Color) {
    val safeTarget = target.coerceAtLeast(1)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.width(64.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { (value / safeTarget).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(7.dp),
            color = tint,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text("${value.toInt()} / $safeTarget g", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun HomeMetric(title: String, value: String, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(23.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(title, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(icon, null, Modifier.size(24.dp), tint = Naka.Violet)
            }
            Spacer(Modifier.height(17.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 23.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(" $unit", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeExercise(title: String, subtitle: String, color: Color, image: Int, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Column {
            Box(Modifier.fillMaxWidth().height(188.dp * LocalDensity.current.fontScale.coerceIn(1f, 1.6f))) {
                NakaPhoto(image, title.replace("\n", " "), Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .78f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(title.replace("\n", " "), color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(subtitle, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().background(color).padding(horizontal = 16.dp, vertical = 10.dp)) {
                @Composable fun Start(modifier: Modifier = Modifier) {
                    Button(onClick = onClick, modifier = modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Naka.Violet, contentColor = Color.White)) {
                        Text("Start Now", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
                    }
                }
                if(maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your next session", color = Naka.Ink, style = MaterialTheme.typography.labelLarge)
                        Start(Modifier.fillMaxWidth())
                    }
                } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Your next session", Modifier.weight(1f).padding(end = 8.dp), color = Naka.Ink,
                        style = MaterialTheme.typography.labelLarge)
                    Start()
                }
            }
        }
    }
}
