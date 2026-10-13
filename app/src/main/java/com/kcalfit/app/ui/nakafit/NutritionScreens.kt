package com.kcalfit.app.ui.nakafit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.time.LocalDate

@Composable
fun NutritionMenuScreen(uiState: CalFitUiState, onMeal: (MealType) -> Unit, onSearch: () -> Unit) {
    NakaPage("Nutrition", action = { IconButton(onSearch) { Icon(Icons.Default.Search, "Search food") } }) {
        NakaPhotoCard("Track Your Nutrition", "Monitor calories and macros", R.drawable.nakafit_food, { onMeal(MealType.BREAKFAST) }, height = 200)
        NakaStat("${uiState.totalCalories} / ${uiState.dailyGoal} kcal", "Calories Intake · ${uiState.selectedDate}",
            Modifier.fillMaxWidth().clickable { onMeal(MealType.BREAKFAST) })
        LinearProgressIndicator(progress = { (uiState.totalCalories.toFloat() / uiState.dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Text("Recorded: %.1f g carbs · %.1f g protein · %.1f g fat".format(uiState.totalCarbsGrams, uiState.totalProteinGrams, uiState.totalFatGrams),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        NakaSection("Your Meals")
        MealType.entries.forEach { meal -> NakaRow(meal.displayName, "${uiState.mealBreakdown[meal.displayName] ?: 0} kcal", { onMeal(meal) }) }
    }
}

@Composable
fun SearchScreen(uiState: CalFitUiState, onBack: () -> Unit, onQuery: (String) -> Unit,
    onMeal: (FoodEntry) -> Unit, onWorkout: (String) -> Unit, onAdd: () -> Unit, onSelectFood: (FoodItemEntity) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Menu") }
    NakaPage("Search", onBack) {
        OutlinedTextField(query, { query = it; onQuery(it) }, Modifier.fillMaxWidth(), placeholder = { Text("Search menu or exercise") },
            leadingIcon = { Icon(Icons.Default.Search,null) }, shape = RoundedCornerShape(30.dp), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Menu", "Exercise").forEach { item -> FilterChip(category == item, { category = item }, label = { Text(item) }) }
        }
        if (category == "Exercise") {
            val results = nakaWorkouts.filter { it.title.contains(query,true) || it.category.contains(query,true) }
            results.forEach { NakaPhotoCard(it.title,"${it.minutes} min · ${it.category}",it.image,{ onWorkout(it.id) }) }
            if (results.isEmpty()) Text("No matching exercises", color = Naka.Muted)
        } else {
            val samples = uiState.allEntries.filter { it.foodName.contains(query,true) }
            samples.forEach { meal -> NakaRow(meal.foodName,"${meal.calories} kcal · Recorded entry", { onMeal(meal) }) }
            uiState.masterFoodSearchResults.take(20).forEach { food ->
                NakaRow(food.name,"${food.calories} kcal · ${food.brand}", { onSelectFood(food) })
            }
            if (samples.isEmpty() && uiState.masterFoodSearchResults.isEmpty()) Text("No matching meals", color = Naka.Muted)
            NakaButton("Add Food Manually", onAdd, outlined = true)
        }
    }
}

