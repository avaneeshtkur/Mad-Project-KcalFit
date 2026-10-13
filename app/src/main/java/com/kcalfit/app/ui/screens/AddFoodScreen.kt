package com.kcalfit.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.nakafit.*
import java.time.LocalDate

@Composable
fun AddFoodScreen(initialMealType: MealType = MealType.BREAKFAST, searchResults: List<FoodItemEntity>,
    onSearchQueryChange: (String) -> Unit,
    onAddFoodSubmit: (String, String, Int, Double, Double, Double, Double, Double, Double, String, String) -> Unit,
    onNavigateBack: () -> Unit, initialFood: FoodItemEntity? = null, initialDate: String = LocalDate.now().toString(),
    modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var query by remember(initialFood) { mutableStateOf(initialFood?.name.orEmpty()) }
    var selected by remember(initialFood) { mutableStateOf(initialFood) }
    var meal by remember { mutableStateOf(initialMealType) }
    var servings by remember { mutableStateOf("1") }
    var date by remember { mutableStateOf(initialDate) }
    var manual by remember { mutableStateOf(false) }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("0") }
    var carbs by remember { mutableStateOf("0") }
    var fat by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf<String?>(null) }
    val foodFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )
    NakaPage("Add Food", onNavigateBack) {
        Text("Search your food or enter a meal manually.", color = Naka.Muted)
        OutlinedTextField(query, { query = it; selected = null; onSearchQueryChange(it); error = null }, Modifier.fillMaxWidth(),
            label = { Text("Food name") }, singleLine = true, shape = RoundedCornerShape(28.dp), colors = foodFieldColors)
        if (selected == null && !manual && query.isNotBlank()) {
            if (searchResults.isEmpty()) Text("No matching food found", color = Naka.Muted)
            searchResults.take(20).forEach { food -> NakaRow(food.name, "${food.calories} kcal · ${food.servingSize} ${food.servingUnit}", {
                selected = food; query = food.name
            }) }
        }
        TextButton({ manual = !manual; selected = null }) { Text(if (manual) "Use Food Search" else "Enter Nutrition Manually", color = Naka.Violet) }
        if (manual) {
            listOf("Calories (kcal)" to calories, "Protein (g)" to protein, "Carbs (g)" to carbs, "Fat (g)" to fat).forEachIndexed { index,(label,value) ->
                OutlinedTextField(value, { when(index) { 0 -> calories=it; 1 -> protein=it; 2 -> carbs=it; else -> fat=it } },
                    Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(28.dp), colors = foodFieldColors)
            }
        }
        // Keep the selector geometry stable in both states and on narrow phones.
        MealType.entries.chunked(2).forEach { rowMeals ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMeals.forEach { type ->
                    FilterChip(
                        selected = meal == type,
                        onClick = { meal = type },
                        modifier = Modifier.weight(1f),
                        label = { Text(type.displayName, maxLines = 1) }
                    )
                }
                if (rowMeals.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        NakaRow("Date", date, {
            val parsed = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now())
            DatePickerDialog(context, { _, year, month, day -> date=LocalDate.of(year,month+1,day).toString() },
                parsed.year,parsed.monthValue-1,parsed.dayOfMonth).show()
        })
        if (selected != null) {
            Text(selected!!.name, style = MaterialTheme.typography.titleLarge)
            Text("${selected!!.calories} kcal · P ${selected!!.proteinGrams}g · C ${selected!!.carbsGrams}g · F ${selected!!.fatGrams}g per serving", color=Naka.Muted)
            OutlinedTextField(servings,{ servings=it },Modifier.fillMaxWidth(),label={ Text("Servings") },
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),shape=RoundedCornerShape(28.dp), colors=foodFieldColors)
        }
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        NakaButton("Add to ${meal.displayName}", {
            if (manual) {
                val c=calories.toIntOrNull();val p=protein.toDoubleOrNull();val cb=carbs.toDoubleOrNull();val f=fat.toDoubleOrNull()
                if (query.isBlank() || c==null || c<0 || p==null || cb==null || f==null || p<0 || cb<0 || f<0) error="Enter a food name and valid nutrition values"
                else onAddFoodSubmit(query.trim(),meal.displayName,c,p,cb,f,0.0,0.0,0.0,"1 serving",date)
            } else {
                val food=selected;val multiplier=servings.toDoubleOrNull()
                if (food==null) error="Choose a food first"
                else if (multiplier==null || !multiplier.isFinite() || multiplier<=0 || multiplier>100) error="Enter a serving count between 0 and 100"
                else onAddFoodSubmit(food.name,meal.displayName,(food.calories*multiplier).toInt(),food.proteinGrams*multiplier,
                    food.carbsGrams*multiplier,food.fatGrams*multiplier,food.fiberGrams*multiplier,food.sugarGrams*multiplier,
                    food.sodiumMg*multiplier,"$multiplier servings",date)
            }
        })
    }
}
