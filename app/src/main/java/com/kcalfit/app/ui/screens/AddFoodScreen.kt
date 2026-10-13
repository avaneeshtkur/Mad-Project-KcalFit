package com.kcalfit.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.*
import com.kcalfit.app.domain.calculator.FoodDiaryRules
import com.kcalfit.app.ui.nakafit.*
import java.time.LocalDate

@Composable
fun AddFoodScreen(initialMealType: MealType = MealType.BREAKFAST, searchResults: List<FoodItemEntity>,
    onSearchQueryChange: (String) -> Unit, onSave: (FoodEntry, (String?) -> Unit) -> Unit,
    onNavigateBack: () -> Unit, initialFood: FoodItemEntity? = null, initialDate: String = LocalDate.now().toString(),
    modifier: Modifier = Modifier, onSaved: (FoodEntry) -> Unit = { onNavigateBack() }) {
    val context = LocalContext.current
    var query by rememberSaveable(initialFood?.id) { mutableStateOf(initialFood?.name.orEmpty()) }
    var selected by remember(initialFood) { mutableStateOf(initialFood) }
    var meal by rememberSaveable(initialMealType) { mutableStateOf(initialMealType) }
    var amount by rememberSaveable { mutableStateOf(initialFood?.servingSize?.toString() ?: "1") }
    var unit by rememberSaveable { mutableStateOf(initialFood?.servingUnit ?: "serving") }
    var date by rememberSaveable { mutableStateOf(initialDate) }
    var manual by rememberSaveable { mutableStateOf(false) }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val fields = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline, cursorColor = MaterialTheme.colorScheme.primary)
    NakaPage("Add Food", onNavigateBack) {
        OutlinedTextField(query, { query = it; selected = null; onSearchQueryChange(it); error = null }, Modifier.fillMaxWidth(),
            label = { Text("Food name") }, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge, colors = fields, shape = RoundedCornerShape(28.dp))
        if (!manual && selected == null && query.isNotBlank()) {
            if (searchResults.isEmpty()) Text("No matching food. Enter nutrition manually.")
            searchResults.take(20).forEach { food -> NakaRow(food.name,
                "${food.calories} kcal / ${food.servingSize} ${food.servingUnit}", {
                    selected = food; query = food.name; amount = food.servingSize.toString(); unit = food.servingUnit
                }) }
        }
        TextButton({ manual = !manual; selected = null }) { Text(if (manual) "Use Food Search" else "Enter Nutrition Manually") }
        Text("Serving", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(amount, { amount = it }, Modifier.weight(1f), label = { Text("Quantity") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = fields)
            OutlinedTextField(unit, { unit = it }, Modifier.weight(1f), label = { Text("Unit (g, ml, serving)") }, singleLine = true,
                enabled = manual, colors = fields)
        }
        if (manual) {
            Text("Nutrition for this portion", style = MaterialTheme.typography.titleMedium)
            Text("Use the label or your own source for the entire quantity above.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf("Calories (kcal)" to calories, "Protein (g)" to protein, "Carbs (g)" to carbs, "Fat (g)" to fat).forEachIndexed { i, (label, value) ->
                OutlinedTextField(value, { when (i) { 0 -> calories = it; 1 -> protein = it; 2 -> carbs = it; else -> fat = it } },
                    Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge, colors = fields,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            Text("Macros are optional. Leave all three blank if unavailable.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else if (selected != null) {
            Text("Catalogue estimates per ${selected!!.servingSize} ${selected!!.servingUnit}. Values scale to your quantity; check the packaging for accuracy.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        MealType.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type -> FilterChip(meal == type, { meal = type }, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    label = { Text(type.displayName, style = MaterialTheme.typography.labelLarge) }, colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer)) }
            }
        }
        NakaRow("Date", date, {
            val d = LocalDate.parse(date)
            DatePickerDialog(context, { _, y, m, day -> date = LocalDate.of(y, m + 1, day).toString() }, d.year, d.monthValue - 1, d.dayOfMonth).show()
        })
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        NakaButton(if (saving) "Saving…" else "Add to ${meal.displayName}", {
            try {
                val quantity = amount.toDoubleOrNull() ?: error("Enter a valid quantity")
                require(quantity.isFinite() && quantity > 0 && unit.isNotBlank()) { "Enter a positive quantity and unit" }
                val known = listOf(protein, carbs, fat).all { it.isNotBlank() }
                require(!manual || known || listOf(protein, carbs, fat).all { it.isBlank() }) { "Enter all three macros or leave all blank" }
                val food = selected
                val factor = if (!manual) FoodDiaryRules.multiplier(quantity, food?.servingSize ?: error("Choose a food first")) else 1.0
                val entry = if (manual) FoodEntry(foodName = query.trim(), mealType = meal.displayName,
                    calories = calories.toIntOrNull() ?: error("Enter calories for this quantity"),
                    proteinGrams = if (known) protein.toDouble() else 0.0, carbsGrams = if (known) carbs.toDouble() else 0.0,
                    fatGrams = if (known) fat.toDouble() else 0.0, quantity = "$amount ${unit.trim()}", date = date, nutritionKnown = known)
                else FoodEntry(foodName = food!!.name, brand = food.brand, mealType = meal.displayName,
                    calories = FoodDiaryRules.calories(food.calories, factor), proteinGrams = food.proteinGrams * factor,
                    carbsGrams = food.carbsGrams * factor, fatGrams = food.fatGrams * factor, fiberGrams = food.fiberGrams * factor,
                    sugarGrams = food.sugarGrams * factor, sodiumMg = food.sodiumMg * factor, quantity = "$amount $unit", date = date,
                    nutritionKnown = food.nutritionKnown)
                FoodDiaryRules.validate(entry)
                saving = true; error = null
                onSave(entry) { failure -> saving = false; if (failure == null) onSaved(entry) else error = failure }
            } catch (e: Exception) { error = e.message ?: "Check the food values" }
        }, enabled = !saving)
    }
}
