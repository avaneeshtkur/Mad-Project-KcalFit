package com.kcalfit.app.ui.reference

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.ui.theme.CalFitTheme
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.time.LocalDate

/** Debug-only fixtures for inspecting the local UI kit implementation without an emulator. */
private fun previewState(): CalFitUiState {
    val date = LocalDate.now().toString()
    val foods = listOf(
        FoodEntry(id = 1, foodName = "Oats with banana", mealType = "Breakfast", calories = 380, proteinGrams = 12.0, carbsGrams = 65.0, fatGrams = 8.0, date = date),
        FoodEntry(id = 2, foodName = "Rice bowl", mealType = "Lunch", calories = 520, proteinGrams = 28.0, carbsGrams = 72.0, fatGrams = 13.0, date = date),
        FoodEntry(id = 3, foodName = "Lentil soup", mealType = "Dinner", calories = 280, proteinGrams = 18.0, carbsGrams = 42.0, fatGrams = 6.0, date = date),
        FoodEntry(id = 4, foodName = "Yogurt", mealType = "Snack", calories = 120, proteinGrams = 8.0, carbsGrams = 14.0, fatGrams = 3.0, date = date)
    )
    return CalFitUiState(
        userProfile = UserEntity(name = "Alex"), selectedDate = date,
        selectedDateEntries = foods, allEntries = foods, dailyGoal = 1800,
        totalCalories = foods.sumOf { it.calories }, caloriesBurned = 215,
        totalProteinGrams = foods.sumOf { it.proteinGrams },
        totalCarbsGrams = foods.sumOf { it.carbsGrams },
        totalFatGrams = foods.sumOf { it.fatGrams }, latestWeightKg = 68.5
    )
}

@Preview(name = "Nutrition · local kit", widthDp = 360, heightDp = 760, showBackground = true)
@Preview(name = "Nutrition · dark", widthDp = 360, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Nutrition · large text", widthDp = 360, heightDp = 760, fontScale = 1.5f)
@Composable
private fun NutritionPreview() {
    CalFitTheme { Surface { ReferenceNutrition(previewState(), {}, {}, {}, {}) } }
}

@Preview(name = "Nutrition · empty", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun EmptyNutritionPreview() {
    CalFitTheme { Surface { ReferenceNutrition(CalFitUiState(), {}, {}, {}, {}) } }
}

@Preview(name = "Dashboard · local kit", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun DashboardPreview() {
    CalFitTheme { Surface { ReferenceDashboard(previewState(), {}, {}, steps = 449L) } }
}

@Preview(name = "Diary · local kit", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun DiaryPreview() {
    CalFitTheme { Surface { ReferenceDiary(previewState(), {}, {}, {}, {}, {}, {}, {}) } }
}
