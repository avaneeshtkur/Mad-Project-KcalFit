package com.kcalfit.app.ui.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.components.CalorieGoalDialog
import com.kcalfit.app.ui.components.FoodEntryItem
import com.kcalfit.app.ui.components.MealSummaryCard
import com.kcalfit.app.ui.theme.CoralAccent
import com.kcalfit.app.ui.theme.GreenPrimary
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    uiState: CalFitUiState,
    onAddFoodClick: () -> Unit,
    onQuickAddClick: () -> Unit,
    onBarcodeClick: () -> Unit,
    onMealScanClick: () -> Unit,
    onVoiceLogClick: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onAddWaterClick: (Int) -> Unit,
    onLogWeightClick: () -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    onUpdateGoal: (Int) -> Unit,
    onMealCategoryClick: (MealType) -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalDialog by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        dateFormat.format(Date())
    }

    if (showGoalDialog) {
        CalorieGoalDialog(
            currentGoal = uiState.dailyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onUpdateGoal(newGoal)
                showGoalDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Cal Fit",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddFoodClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Food")
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = "Add Food", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Calorie & Net Intake Ring Card
            item {
                TodayCalorieRingCard(
                    goal = uiState.dailyGoal,
                    consumed = uiState.totalCalories,
                    burned = uiState.caloriesBurned,
                    net = uiState.netCalories,
                    remaining = uiState.remainingCalories,
                    isOver = uiState.isOverGoal,
                    excess = uiState.excessCalories,
                    onEditGoalClick = { showGoalDialog = true }
                )
            }

            // 2. Macro Intake Bars (Protein, Carbs, Fat)
            item {
                TodayMacroBarsCard(
                    consumedProtein = uiState.totalProteinGrams.toInt(),
                    targetProtein = uiState.targetProteinGrams,
                    consumedCarbs = uiState.totalCarbsGrams.toInt(),
                    targetCarbs = uiState.targetCarbsGrams,
                    consumedFat = uiState.totalFatGrams.toInt(),
                    targetFat = uiState.targetFatGrams
                )
            }

            // 3. Quick Action Buttons Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quick Logging Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(label = "Barcode", icon = Icons.Default.QrCodeScanner, onClick = onBarcodeClick)
                        QuickActionButton(label = "Meal Scan", icon = Icons.Default.CameraAlt, onClick = onMealScanClick)
                        QuickActionButton(label = "Voice Log", icon = Icons.Default.Mic, onClick = onVoiceLogClick)
                        QuickActionButton(label = "Quick Add", icon = Icons.Default.Add, onClick = onQuickAddClick)
                        QuickActionButton(label = "Exercise", icon = Icons.Default.DirectionsRun, onClick = onAddExerciseClick)
                        QuickActionButton(label = "Water", icon = Icons.Default.WaterDrop, onClick = { onAddWaterClick(250) })
                        QuickActionButton(label = "Weight", icon = Icons.Default.MonitorWeight, onClick = onLogWeightClick)
                    }
                }
            }

            // 4. Meal Breakdown Summary Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Meals Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MealSummaryCard(
                            mealType = MealType.BREAKFAST,
                            calories = uiState.mealBreakdown[MealType.BREAKFAST.displayName] ?: 0,
                            onClick = { onMealCategoryClick(MealType.BREAKFAST) },
                            modifier = Modifier.weight(1f)
                        )
                        MealSummaryCard(
                            mealType = MealType.LUNCH,
                            calories = uiState.mealBreakdown[MealType.LUNCH.displayName] ?: 0,
                            onClick = { onMealCategoryClick(MealType.LUNCH) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MealSummaryCard(
                            mealType = MealType.DINNER,
                            calories = uiState.mealBreakdown[MealType.DINNER.displayName] ?: 0,
                            onClick = { onMealCategoryClick(MealType.DINNER) },
                            modifier = Modifier.weight(1f)
                        )
                        MealSummaryCard(
                            mealType = MealType.SNACK,
                            calories = uiState.mealBreakdown[MealType.SNACK.displayName] ?: 0,
                            onClick = { onMealCategoryClick(MealType.SNACK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Today's Food Entries
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Food Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${uiState.selectedDateEntries.size} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.selectedDateEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No foods logged for today yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onAddFoodClick) {
                                Text("+ Log First Meal")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.selectedDateEntries,
                    key = { it.id }
                ) { entry ->
                    FoodEntryItem(
                        foodEntry = entry,
                        onDeleteClick = { onDeleteEntry(entry) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun TodayCalorieRingCard(
    goal: Int, consumed: Int, burned: Int, net: Int, remaining: Int, isOver: Boolean, excess: Int,
    onEditGoalClick: () -> Unit
) {
    val progress = if (goal > 0) (net.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800), label = "ring")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Calorie Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onEditGoalClick, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Goal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(105.dp)) {
                    CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(98.dp), color = MaterialTheme.colorScheme.surfaceVariant, strokeWidth = 9.dp)
                    CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(98.dp), color = if (isOver) CoralAccent else GreenPrimary, strokeWidth = 9.dp)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$net", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text("net kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(modifier = Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalorieStatText("Goal", "$goal kcal", MaterialTheme.colorScheme.onSurface)
                    CalorieStatText("Food", "+$consumed kcal", GreenPrimary)
                    CalorieStatText("Exercise", "-$burned kcal", CoralAccent)
                    CalorieStatText(if (isOver) "Status" else "Remaining", if (isOver) "+$excess kcal over" else "$remaining kcal", if (isOver) CoralAccent else GreenPrimary)
                }
            }
        }
    }
}

@Composable
fun TodayMacroBarsCard(consumedProtein: Int, targetProtein: Int, consumedCarbs: Int, targetCarbs: Int, consumedFat: Int, targetFat: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Macronutrient Targets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            MacroBarRow("Protein", consumedProtein, targetProtein, Color(0xFFF59E0B))
            MacroBarRow("Carbs", consumedCarbs, targetCarbs, Color(0xFF10B981))
            MacroBarRow("Fat", consumedFat, targetFat, Color(0xFFEC4899))
        }
    }
}

@Composable
fun MacroBarRow(label: String, consumed: Int, target: Int, color: Color) {
    val progress = if (target > 0) (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text("$consumed / $target g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CalorieStatText(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.width(180.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
