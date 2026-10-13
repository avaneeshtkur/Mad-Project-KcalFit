package com.kcalfit.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFoodScreen(
    initialMealType: MealType = MealType.BREAKFAST,
    searchResults: List<FoodItemEntity>,
    onSearchQueryChange: (String) -> Unit,
    onAddFoodSubmit: (foodName: String, mealType: String, calories: Int, protein: Double, carbs: Double, fat: Double, fiber: Double, sugar: Double, sodium: Double, quantity: String, date: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<FoodItemEntity?>(null) }
    var meal by remember(initialMealType) { mutableStateOf(initialMealType) }
    var quantity by remember { mutableStateOf("1") }
    val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val multiplier = quantity.toDoubleOrNull()?.coerceAtLeast(0.1) ?: 1.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add food") },
                navigationIcon = { IconButton(onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }, modifier = modifier
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Search your food database", style = MaterialTheme.typography.headlineSmall)
                Text("Choose a match to import its calories and nutrition automatically.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; selectedFood = null; onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search chicken, rice, paneer...") },
                    label = { Text("Food name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            if (query.isNotBlank() && selectedFood == null) {
                if (searchResults.isEmpty()) item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("No match found", Modifier.padding(18.dp), style = MaterialTheme.typography.titleMedium)
                    }
                } else items(searchResults.take(30), key = { it.id }) { food ->
                    FoodSearchResult(food) {
                        selectedFood = food
                        query = food.name
                    }
                }
            }
            item {
                Text("Meal", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.forEach { type ->
                        FilterChip(selected = meal == type, onClick = { meal = type }, label = { Text(if (type == MealType.SNACK) "Snacks" else type.displayName) })
                    }
                }
            }
            selectedFood?.let { food ->
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(food.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("${food.servingSize} ${food.servingUnit} per serving", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            NutritionGrid(food, multiplier)
                            OutlinedTextField(quantity, { quantity = it }, label = { Text("Servings") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    onAddFoodSubmit(food.name, meal.displayName,
                                        (food.calories * multiplier).toInt(), food.proteinGrams * multiplier,
                                        food.carbsGrams * multiplier, food.fatGrams * multiplier,
                                        food.fiberGrams * multiplier, food.sugarGrams * multiplier,
                                        food.sodiumMg * multiplier, "$multiplier serving$", date)
                                    onNavigateBack()
                                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                            ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add to ${meal.displayName}") }
                        }
                    }
                }
            }
            if (query.isBlank()) item {
                Text("Nutrition values are taken from the selected database serving. Search first to add a verified item.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FoodSearchResult(food: FoodItemEntity, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleMedium)
                Text("${food.servingSize} ${food.servingUnit}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Column { Text("${food.calories} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold); Text("P ${food.proteinGrams}g · C ${food.carbsGrams}g · F ${food.fatGrams}g", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun NutritionGrid(food: FoodItemEntity, multiplier: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Nutrition for ${"%.1f".format(multiplier)} serving${if (multiplier == 1.0) "" else "s"}", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { NutritionValue("Calories", "${(food.calories * multiplier).toInt()} kcal"); NutritionValue("Protein", "${"%.1f".format(food.proteinGrams * multiplier)} g"); NutritionValue("Carbs", "${"%.1f".format(food.carbsGrams * multiplier)} g") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { NutritionValue("Fat", "${"%.1f".format(food.fatGrams * multiplier)} g"); NutritionValue("Fiber", "${"%.1f".format(food.fiberGrams * multiplier)} g"); NutritionValue("Sugar", "${"%.1f".format(food.sugarGrams * multiplier)} g") }
        Text("Sodium: ${"%.0f".format(food.sodiumMg * multiplier)} mg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun NutritionValue(label: String, value: String) { Column { Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontWeight = FontWeight.SemiBold) } }
