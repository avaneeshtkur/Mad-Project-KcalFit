package com.kcalfit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.R
import com.kcalfit.app.ui.nakafit.*

@Composable
fun MealDetailScreen(entry: FoodEntry?, onBack: () -> Unit) {
    NakaPage("Food Details", onBack) {
        if (entry == null) {
            Text("Entry unavailable", style = MaterialTheme.typography.titleLarge)
            Text("This record may have been deleted. Return to the food diary.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            NakaPhoto(R.drawable.nakafit_food, "Nutrition illustration, not the recorded dish",
                Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp)))
            Text("Nutrition illustration", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(entry.foodName, style = MaterialTheme.typography.titleLarge)
            Text("${entry.mealType} · ${entry.date}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            NakaStat("${entry.calories} kcal", entry.quantity, Modifier.fillMaxWidth())
            if (entry.nutritionKnown) {
                val macros = listOf("Carbohydrates" to entry.carbsGrams, "Protein" to entry.proteinGrams, "Fat" to entry.fatGrams)
                val total = macros.sumOf { it.second }.coerceAtLeast(1.0)
                macros.forEach { (name, value) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, Modifier.weight(1f).padding(end = 12.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.1f g".format(value), style = MaterialTheme.typography.titleMedium)
                    }
                    LinearProgressIndicator(progress = { (value / total).toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                }
            } else Text("Macro information is incomplete or unavailable for this portion.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Quantity", style = MaterialTheme.typography.titleMedium)
            Text(entry.quantity, style = MaterialTheme.typography.bodyLarge)
            Text("Nutrition reflects the recorded portion. Ingredients were not recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
