package com.kcalfit.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.ui.nakafit.NakaPhoto
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.time.LocalDate

private val Ink = Color(0xFF101014)
private val Violet = Color(0xFF6500F5)
private val Muted = Color(0xFF818AA0)
private val Pale = Color(0xFFF8F8F9)

@Composable
fun NutritionScreen(
    uiState: CalFitUiState,
    onDateSelected: (String) -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    onAddFood: () -> Unit = {},
    onScanBarcode: () -> Unit = {},
    onMealScan: () -> Unit = {},
    onBack: () -> Unit = {},
    onMealClick: (FoodEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dayOpen by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()) }
    val breakfastDates = uiState.allEntries.filter { MealType.fromString(it.mealType) == MealType.BREAKFAST }
        .map { it.date }.distinct().sortedDescending()

    LazyColumn(modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(55.dp), verticalAlignment = Alignment.CenterVertically) {
                if (dayOpen) IconButton(onClick = { dayOpen = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                else IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                Text(if (dayOpen) runCatching { displayFormat.format(dateFormat.parse(uiState.selectedDate)!!) }.getOrDefault(uiState.selectedDate)
                    else "Breakfast", Modifier.weight(1f), color = Ink, fontSize = 19.sp)
                IconButton(onClick = onAddFood) { Icon(Icons.Default.Search, "Search food") }
            }
        }
        if (!dayOpen) {
            item {
                Surface(Modifier.fillMaxWidth().height(180.dp).clickable { dayOpen = true },
                    shape = RoundedCornerShape(24.dp), color = Color(0xFFF1FFD2)) {
                    Box {
                        NakaPhoto(R.drawable.nakafit_food, "Healthy meal", Modifier.fillMaxHeight().fillMaxWidth(.43f).align(Alignment.CenterEnd))
                        Column(Modifier.padding(23.dp), verticalArrangement = Arrangement.Center) {
                        Text("Track Your\nNutrition", color = Ink, fontSize = 23.sp, lineHeight = 27.sp)
                        Text("Monitor calories, macros,\nand vitamins.", color = Ink, fontSize = 12.sp)
                        Spacer(Modifier.height(13.dp))
                        Surface(color = Ink, shape = RoundedCornerShape(18.dp)) {
                            Text("Start Now", Modifier.padding(horizontal = 16.dp, vertical = 7.dp), color = Color.White, fontSize = 12.sp)
                        }
                        }
                    }
                }
            }
            val dates = (listOf(uiState.selectedDate) + breakfastDates + (0..6).map { LocalDate.now().minusDays(it.toLong()).toString() }).distinct().sortedDescending().take(30)
            items(dates) { date ->
                val entries = uiState.allEntries.filter { it.date == date && MealType.fromString(it.mealType) == MealType.BREAKFAST }
                Surface(Modifier.fillMaxWidth().clickable { onDateSelected(date); dayOpen = true },
                    shape = RoundedCornerShape(24.dp), color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5EAF0))) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val parsed = runCatching { dateFormat.parse(date) }.getOrNull()
                            val calendar = Calendar.getInstance().apply { if (parsed != null) time = parsed }
                            Column(Modifier.weight(1f)) {
                                Text(SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time), fontSize = 18.sp, color = Ink)
                                Text(SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(calendar.time), fontSize = 13.sp, color = Muted)
                            }
                            Surface(Modifier.size(42.dp).clickable { onDateSelected(date); onAddFood() },
                                shape = CircleShape, color = Color(0xFFE0CEFF)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, "Add breakfast", tint = Ink) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (entries.isEmpty()) Text("No breakfast logged yet", color = Muted, fontSize = 13.sp)
                        else entries.take(2).forEach { entry ->
                            Text("${entry.foodName} · ${entry.calories} kcal", Modifier.padding(vertical = 5.dp),
                                color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        } else {
            item {
                val entries = uiState.selectedDateEntries.filter { MealType.fromString(it.mealType) == MealType.BREAKFAST }
                val calories = entries.sumOf { it.calories }
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Pale) {
                    Column(Modifier.padding(17.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text("Calories Intake", Modifier.weight(1f), color = Ink)
                            Text("${calories}kcal", color = Muted, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = { (calories.toFloat() / uiState.dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(11.dp), color = Violet, trackColor = Color.White)
                        Spacer(Modifier.height(22.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MacroRing("Carbs", entries.sumOf { it.carbsGrams }, uiState.targetCarbsGrams.toDouble(), Color(0xFFD9BBFF), Modifier.weight(1f))
                            MacroRing("Protein", entries.sumOf { it.proteinGrams }, uiState.targetProteinGrams.toDouble(), Color(0xFFFFD975), Modifier.weight(1f))
                            MacroRing("Fat", entries.sumOf { it.fatGrams }, uiState.targetFatGrams.toDouble(), Color(0xFFFFA8AC), Modifier.weight(1f))
                            MacroRing("Fiber", entries.sumOf { it.fiberGrams }, 30.0, Color(0xFFA5D727), Modifier.weight(1f))
                        }
                    }
                }
            }
            val entries = uiState.selectedDateEntries.filter { MealType.fromString(it.mealType) == MealType.BREAKFAST }
            if (entries.isEmpty()) item { Text("No breakfast logged for this day", color = Muted) }
            items(entries, key = { it.id }) { entry ->
                Surface(Modifier.fillMaxWidth().clickable { onMealClick(entry) }, shape = RoundedCornerShape(23.dp),
                    color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5EAF0))) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(76.dp).background(Pale, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            NakaPhoto(R.drawable.nakafit_food, "Food photograph", Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
                        }
                        Spacer(Modifier.width(15.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.foodName, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${entry.calories}kcal", color = Ink, fontSize = 13.sp)
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onMealScan, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Scan a Meal") }
                    Button(onClick = onScanBarcode, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Scan Barcode") }
                }
            }
        }
    }
}

@Composable
private fun MacroRing(label: String, amount: Double, target: Double, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { (amount / target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                color = color, trackColor = Color(0xFFE9ECEF), strokeWidth = 7.dp)
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = Ink, fontSize = 12.sp)
        Text("${amount.toInt()}g", color = Muted, fontSize = 11.sp)
    }
}
