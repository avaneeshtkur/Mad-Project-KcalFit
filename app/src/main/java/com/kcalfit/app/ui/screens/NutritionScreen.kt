package com.kcalfit.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kcalfit.app.R
import com.kcalfit.app.data.model.*
import com.kcalfit.app.domain.calculator.FoodDiaryRules
import com.kcalfit.app.ui.nakafit.*
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NutritionScreen(uiState: CalFitUiState, onDateSelected: (String) -> Unit, onDeleteEntry: (FoodEntry) -> Unit,
    onAddFood: () -> Unit = {}, onScanBarcode: () -> Unit = {},
    onBack: () -> Unit = {}, onMealClick: (FoodEntry) -> Unit = {}, modifier: Modifier = Modifier,
    meal: MealType = MealType.BREAKFAST) {
    val context = LocalContext.current
    val selected = runCatching { LocalDate.parse(uiState.selectedDate) }.getOrDefault(LocalDate.now())
    val entries = FoodDiaryRules.entriesFor(uiState.selectedDateEntries, uiState.selectedDate, meal)
    val week = FoodDiaryRules.week(selected)
    val totals = FoodDiaryRules.totals(entries)
    NakaPage(meal.displayName, onBack) {
        NakaPhotoCard("Track Your Nutrition", "${entries.size} entries · ${entries.sumOf { it.calories }} kcal",
            R.drawable.nakafit_food, onAddFood, height = 180)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ onDateSelected(selected.minusWeeks(1).toString()) }) { Icon(Icons.Default.ChevronLeft, "Previous week") }
            TextButton(onClick = {
                DatePickerDialog(context, { _, y, m, d -> onDateSelected(LocalDate.of(y, m + 1, d).toString()) },
                    selected.year, selected.monthValue - 1, selected.dayOfMonth).show()
            }, modifier = Modifier.weight(1f)) { Text(selected.format(DateTimeFormatter.ofPattern("d MMM yyyy"))) }
            IconButton({ onDateSelected(selected.plusWeeks(1).toString()) }) { Icon(Icons.Default.ChevronRight, "Next week") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            week.forEach { date ->
                Surface(onClick = { onDateSelected(date.toString()) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    color = if (date == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (date == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.format(DateTimeFormatter.ofPattern("EEE")), style = MaterialTheme.typography.labelSmall)
                        Text("${date.dayOfMonth}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        TextButton({ onDateSelected(LocalDate.now().toString()) }) { Text("Today") }
        NakaStat("${entries.sumOf { it.calories }} kcal", "${meal.displayName} · ${uiState.selectedDate}", Modifier.fillMaxWidth())
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            @Composable fun Nutrient(label: String, grams: Double, modifier: Modifier = Modifier) {
                Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("%.1f g".format(grams), style = MaterialTheme.typography.titleMedium)
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if(maxWidth < 300.dp || LocalDensity.current.fontScale > 1.3f) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Nutrient("Protein", totals.protein, Modifier.fillMaxWidth())
                Nutrient("Carbohydrates", totals.carbs, Modifier.fillMaxWidth())
                Nutrient("Fat", totals.fat, Modifier.fillMaxWidth())
            } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Nutrient("Protein", totals.protein, Modifier.weight(1f))
                Nutrient("Carbs", totals.carbs, Modifier.weight(1f))
                Nutrient("Fat", totals.fat, Modifier.weight(1f))
            }
        }
        if (entries.any { !it.nutritionKnown }) Text("Macro totals include only recorded values; some entries have unavailable nutrition.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        NakaSection("Your Entries")
        if (entries.isEmpty()) Text("No ${meal.displayName.lowercase()} entries for this date.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        entries.forEach { MealEntryRow(it, onMealClick, onDeleteEntry) }
        NakaButton("Add Food to ${meal.displayName}", onAddFood)
        NakaButton("Find Food by Barcode", onScanBarcode, outlined = true)
    }
}
