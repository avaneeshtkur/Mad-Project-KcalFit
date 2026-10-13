package com.kcalfit.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.components.CalFitLogo
import com.kcalfit.app.ui.components.CalorieGoalDialog
import com.kcalfit.app.ui.components.CalorieProgressCard
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: CalFitUiState,
    onAddFoodClick: (MealType?) -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    onUpdateGoal: (Int) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onOpenExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    if (showGoalDialog) CalorieGoalDialog(
        currentGoal = uiState.dailyGoal,
        onDismiss = { showGoalDialog = false },
        onConfirm = { onUpdateGoal(it); showGoalDialog = false }
    )
    val date = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }
    val meals = MealType.entries
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CalFitLogo(size = 34.dp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onDarkModeChange(!darkMode) }) {
                    Icon(if (darkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                        if (darkMode) "Use light mode" else "Use dark mode")
                }
            }
            Spacer(Modifier.height(15.dp))
            Text("Today’s overview", style = MaterialTheme.typography.headlineMedium)
            Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            CalorieProgressCard(
                consumedCalories = uiState.totalCalories,
                goalCalories = uiState.dailyGoal,
                remainingCalories = uiState.remainingCalories,
                isOverGoal = uiState.isOverGoal,
                excessCalories = uiState.excessCalories,
                onEditGoalClick = { showGoalDialog = true }
            )
        }
        item {
            Text("Meals breakdown", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meals.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { meal ->
                            val calories = uiState.mealBreakdown[meal.displayName] ?: 0
                            Surface(
                                modifier = Modifier.weight(1f).clickable { onAddFoodClick(meal) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(Modifier.padding(13.dp)) {
                                    Text(if (meal == MealType.SNACK) "Snacks" else meal.displayName,
                                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$calories kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Today’s food log", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { onAddFoodClick(null) }) { Icon(Icons.Default.Add, null); Text("Quick add") }
            }
            Spacer(Modifier.height(6.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    meals.forEachIndexed { index, meal ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        val entries = uiState.selectedDateEntries.filter { MealType.fromString(it.mealType) == meal }
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 11.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (meal == MealType.SNACK) "Snacks" else meal.displayName,
                                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Text("${entries.sumOf { it.calories }} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge)
                            }
                            if (entries.isEmpty()) {
                                Text("No food logged", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            } else entries.forEach { entry ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.foodName, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${entry.calories} kcal", style = MaterialTheme.typography.bodySmall)
                                    IconButton(onClick = { onDeleteEntry(entry) }) {
                                        Icon(Icons.Default.DeleteOutline, "Delete ${entry.foodName}", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            TextButton(onClick = { onAddFoodClick(meal) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Text("Add ${meal.displayName.lowercase()}")
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    Column {
                        Row(Modifier.fillMaxWidth().clickable(onClick = onOpenExercise).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Exercise", style = MaterialTheme.typography.titleMedium)
                                Text(if (uiState.exerciseEntries.isEmpty()) "No exercise logged • Tap to add" else
                                    "${uiState.exerciseEntries.size} sessions • ${uiState.caloriesBurned} kcal burned",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.Add, "Log exercise")
                        }
                        uiState.exerciseEntries.forEach { entry ->
                            Row(Modifier.fillMaxWidth().padding(start = 50.dp, end = 16.dp, bottom = 10.dp)) {
                                Text(entry.exerciseName, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("−${entry.caloriesBurned} kcal", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
