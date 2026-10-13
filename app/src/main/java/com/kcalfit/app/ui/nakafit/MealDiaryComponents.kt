package com.kcalfit.app.ui.nakafit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.domain.calculator.FoodDiaryRules
import com.kcalfit.app.ui.viewmodel.CalFitUiState

@Composable
fun MealEntryRow(entry: FoodEntry, onOpen: (FoodEntry) -> Unit, onDelete: (FoodEntry) -> Unit) {
    var confirm by remember(entry.id) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { onOpen(entry) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 8.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Text(entry.foodName, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text("${entry.quantity} · ${entry.calories} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        IconButton(onClick = { confirm = true }) {
            Icon(Icons.Default.DeleteOutline, "Delete ${entry.foodName}", tint = MaterialTheme.colorScheme.error)
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete entry?") },
        text = { Text("Remove ${entry.foodName} (${entry.quantity}) from ${entry.date}?") },
        confirmButton = { TextButton(onClick = { confirm = false; onDelete(entry) }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
}

@Composable
fun MealDiarySection(uiState: CalFitUiState, onMeal: (MealType) -> Unit, onAdd: (MealType) -> Unit,
    onOpen: (FoodEntry) -> Unit, onDelete: (FoodEntry) -> Unit) {
    NakaSection("Food Diary · ${uiState.selectedDate}")
    MealType.entries.forEach { meal ->
        val entries = FoodDiaryRules.entriesFor(uiState.selectedDateEntries, uiState.selectedDate, meal)
        NakaRow(meal.displayName, "${entries.sumOf { it.calories }} kcal · ${entries.size} entries", { onMeal(meal) })
        if (entries.isEmpty()) Text("No food recorded", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        entries.forEach { MealEntryRow(it, onOpen, onDelete) }
        TextButton(onClick = { onAdd(meal) }) { Text("Add Food to ${meal.displayName}") }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
