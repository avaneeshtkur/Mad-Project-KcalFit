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
    onWorkout: (String) -> Unit = {}, demo: Boolean = false, modifier: Modifier = Modifier) {
    var editGoal by remember { mutableStateOf(false) }
    if (editGoal) CalorieGoalDialog(uiState.dailyGoal, { editGoal = false }, { onUpdateGoal(it); editGoal = false })
    val calorieGoal = uiState.dailyGoal.coerceAtLeast(1)
    val calorieTotal = uiState.totalCalories.coerceAtLeast(0)
    val calorieProgress = (calorieTotal.toFloat() / calorieGoal).coerceIn(0f, 1f)
    LazyColumn(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(48.dp).clickable(onClick = onOpenProfile), shape = CircleShape, color = Naka.Lavender) {
                    Box(contentAlignment = Alignment.Center) { Text(if (demo) "AS" else uiState.userProfile?.name?.take(1)?.uppercase() ?: "N",
                        color = Naka.Violet, fontSize = 18.sp) }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Good Morning", fontSize = 12.sp, color = Naka.Muted)
                    Text(if (demo) "Anna Santos" else uiState.userProfile?.name ?: "Fitness User", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onOpenNotifications, Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                    Icon(Icons.Default.NotificationsNone, "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            if (demo) Text("Demo preview", Modifier.padding(top = 8.dp), color = Naka.Muted, fontSize = 10.sp)
        }
        item {
            Surface(Modifier.fillMaxWidth().clickable(onClick = onOpenTrack), color = Naka.Ink, shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Calorie Intake", color = Color.White, fontSize = 18.sp)
                        Text("$calorieTotal / $calorieGoal kcal", color = Naka.Muted, fontSize = 13.sp)
                    }
                    Box(Modifier.size(75.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { calorieProgress }, Modifier.size(66.dp), color = Color.White,
                            trackColor = Color(0xFF303036), strokeWidth = 5.dp)
                        Text("${(calorieProgress * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            NakaSection("Health Overview", "See Details", onOpenTrack)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeMetric("Calories\nBurnt", if (demo) "1.4k" else "${uiState.caloriesBurned}", "kCal", Icons.Default.LocalFireDepartment, Modifier.weight(1f), onOpenTrack)
                HomeMetric("Distance\nCovered", if (demo) "3.8" else "—", "km", Icons.Default.DirectionsRun, Modifier.weight(1f), onOpenTrack)
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
            NakaSection("Your Nutrition", "See Details", onOpenBreakfast)
            NakaPhotoCard("Track Your Nutrition", "Monitor calories and macros", R.drawable.nakafit_food, onOpenBreakfast, height = 180)
        }
        item {
            NakaSection("Daily Goals", "Add Goals", onOpenGoals)
            NakaRow("Daily calorie target", "${uiState.totalCalories} / ${uiState.dailyGoal} kcal", { editGoal = true })
            MealType.entries.forEach { meal -> NakaRow(meal.displayName, "${uiState.mealBreakdown[meal.displayName] ?: 0} kcal", { onAddFoodClick(meal) }) }
        }
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
    Row(Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(24.dp)).background(color).clickable(onClick = onClick)) {
        Column(Modifier.weight(.58f).fillMaxHeight().padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 16.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = Naka.Ink, fontSize = 20.sp, lineHeight = 24.sp, maxLines = 2)
            Text(subtitle, color = Naka.Ink, fontSize = 12.sp, maxLines = 1)
            Spacer(Modifier.height(14.dp))
            Button(onClick = onClick, modifier = Modifier.widthIn(min = 104.dp).height(36.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Naka.Ink, contentColor = Color.White)) {
                Text("Start Now", fontSize = 11.sp, maxLines = 1)
            }
        }
        NakaPhoto(image, title.replace("\n", " "), Modifier.weight(.42f).fillMaxHeight())
    }
}
