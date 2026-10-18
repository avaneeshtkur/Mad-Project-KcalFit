package com.kcalfit.app.ui.reference

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.*
import com.kcalfit.app.R
import com.kcalfit.app.data.model.*
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale
import kotlin.math.*

@Composable
fun ReferenceNutrition(
    state: CalFitUiState,
    onBack: () -> Unit,
    onDate: (String) -> Unit,
    onSettings: () -> Unit,
    onFood: (FoodEntry) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var weekly by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val end = LocalDate.parse(state.selectedDate)
    val dates = (6 downTo 0).map { end.minusDays(it.toLong()).toString() }
    val entries = if (weekly) state.allEntries.filter { it.date in dates } else state.selectedDateEntries
    val exercise = if (weekly) state.allExerciseEntries.filter { it.date in dates }.sumOf { it.caloriesBurned } else state.caloriesBurned
    val total = entries.sumOf { it.calories }
    val mealTotals = MealType.entries.map { meal ->
        entries.filter { MealType.fromString(it.mealType) == meal }.sumOf { it.calories }
    }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(Modifier.fillMaxSize()) {
        ReferenceHeader("Nutrition", onBack) {
            IconButton({
                val csv = buildString {
                    append("Food,Date,Calories,Protein,Carbs,Fat\n")
                    entries.forEach {
                        append("\"${it.foodName.replace("\"", "\"\"")}\",${it.date},${it.calories},${it.proteinGrams},${it.carbsGrams},${it.fatGrams}\n")
                    }
                }
                context.startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).setType("text/csv").putExtra(Intent.EXTRA_TEXT, csv),
                    "Export nutrition"
                ))
            }) { ReferenceIcon(R.drawable.reference_export, "Export nutrition", tint = muted) }
            IconButton(onSettings) { Icon(Icons.Default.Settings, "Nutrition settings", tint = muted) }
        }
        TabRow(tab, containerColor = MaterialTheme.colorScheme.surface) {
            listOf("Calories", "Nutrients", "Macros").forEachIndexed { index, title ->
                Tab(tab == index, { tab = index; weekly = index == 2 }, text = { Text(title) })
            }
        }
        NutritionPeriod(end, weekly, onDate) { weekly = it }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (tab) {
                0 -> NutritionPanel(Modifier.padding(8.dp)) {
                    MealPie(mealTotals)
                    MealType.entries.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                            row.forEach { meal ->
                                val index = MealType.entries.indexOf(meal)
                                Row(Modifier.weight(1f)) {
                                    Box(Modifier.padding(top = 2.dp).size(16.dp).background(ReferenceStyle.MealColors[index]))
                                    Column(Modifier.padding(start = 6.dp)) {
                                        Text(meal.displayName, fontSize = 12.sp)
                                        Text("${if (total > 0) (mealTotals[index] * 100.0 / total).roundToInt() else 0}% (${mealTotals[index]} cal)", fontSize = 11.sp, color = muted)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(top = 16.dp))
                    NutritionLine("Total Calories", "%,d".format(total))
                    NutritionLine("Net Calories", "%,d".format(total - exercise))
                    NutritionLine("Goal", "%,d".format(state.dailyGoal * if (weekly) 7 else 1), MaterialTheme.colorScheme.primary)
                }
                2 -> {
                    SectionDivider()
                    MacroChart(entries, if (weekly) dates else listOf(state.selectedDate), weekly)
                    SectionDivider()
                    MacroLegend(entries, weekly)
                }
                else -> {
                    NutritionPanel(Modifier.padding(8.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Nutrient", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(if (weekly) "Week total" else "Total", color = muted)
                        }
                        HorizontalDivider()
                        listOf(
                            Triple("Protein", entries.sumOf { it.proteinGrams }, "g"),
                            Triple("Carbohydrates", entries.sumOf { it.carbsGrams }, "g"),
                            Triple("Fat", entries.sumOf { it.fatGrams }, "g"),
                            Triple("Fiber", entries.sumOf { it.fiberGrams }, "g"),
                            Triple("Sugar", entries.sumOf { it.sugarGrams }, "g"),
                            Triple("Sodium", entries.sumOf { it.sodiumMg }, "mg")
                        ).forEach { (label, value, unit) -> NutritionLine(label, "%.1f %s".format(value, unit)) }
                    }
                }
            }
            if (entries.any { !it.nutritionKnown }) {
                Text("Some entries have no macro data. Totals include known values only.", Modifier.padding(16.dp), fontSize = 12.sp, color = muted)
            }
            NutritionPanel(Modifier.padding(8.dp)) {
                Text("Food Highest in Calories", Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                HorizontalDivider()
                if (entries.isEmpty()) {
                    Text("Log food to see your nutrition breakdown.", Modifier.padding(16.dp), color = muted)
                }
                entries.sortedByDescending { it.calories }.take(10).forEach { food ->
                    Row(Modifier.fillMaxWidth().clickable { onFood(food) }.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(food.foodName, Modifier.weight(1f))
                        Text("${food.calories} cal", color = muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionPeriod(date: LocalDate, weekly: Boolean, onDate: (String) -> Unit, onWeekly: (Boolean) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()) }
    val label = when {
        date == LocalDate.now() -> if (weekly) "Last 7 Days" else "Today"
        weekly -> "${date.minusDays(6).format(formatter)} – ${date.format(formatter)}"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault()))
    }
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        IconButton({ onDate(date.minusDays(if (weekly) 7 else 1).toString()) }, Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous ${if (weekly) "week" else "day"}")
        }
        VerticalDivider()
        Box(Modifier.weight(1f)) {
            Column(
                Modifier.fillMaxWidth().clickable { menu = true }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (weekly) "Week view" else "Daily view", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(label, fontSize = 16.sp)
            }
            DropdownMenu(menu, { menu = false }) {
                listOf(false, true).forEach { week ->
                    DropdownMenuItem(text = { Text(if (week) "Week view" else "Daily view") }, onClick = { onWeekly(week); menu = false })
                }
            }
        }
        VerticalDivider()
        IconButton({ onDate(date.plusDays(if (weekly) 7 else 1).toString()) }, Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next ${if (weekly) "week" else "day"}")
        }
    }
    HorizontalDivider()
}

@Composable
private fun MealPie(values: List<Int>) {
    val total = values.sum()
    val measurer = rememberTextMeasurer()
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val description = MealType.entries.mapIndexed { index, meal -> "${meal.displayName}: ${values[index]} calories" }.joinToString(", ")
    Box(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp).semantics { contentDescription = description }) {
            if (total == 0) drawCircle(emptyColor)
            var start = -90f
            values.forEachIndexed { index, value ->
                val sweep = if (total > 0) value * 360f / total else 0f
                drawArc(ReferenceStyle.MealColors[index], start, sweep, true)
                // Small slices remain readable in the legend instead of colliding on the pie.
                if (sweep >= 25f) {
                    val angle = Math.toRadians((start + sweep / 2).toDouble())
                    val label = measurer.measure("${(value * 100.0 / total).roundToInt()}%", TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                    val center = Offset(size.width / 2 + cos(angle).toFloat() * size.width * .3f, size.height / 2 + sin(angle).toFloat() * size.height * .3f)
                    drawText(label, topLeft = center - Offset(label.size.width / 2f, label.size.height / 2f))
                }
                start += sweep
            }
        }
        if (total == 0) Text("No food logged", fontSize = 13.sp)
    }
}

@Composable
private fun MacroChart(entries: List<FoodEntry>, dates: List<String>, weekly: Boolean) {
    // Keep unlogged dates in the series; AVG describes all seven days of the selected week.
    val values = dates.map { date ->
        val foods = entries.filter { it.date == date }
        listOf(foods.sumOf { it.proteinGrams }, foods.sumOf { it.fatGrams }, foods.sumOf { it.carbsGrams })
    }.let { days -> if (weekly) days + listOf((0..2).map { index -> days.sumOf { it[index] } / dates.size }) else days }
    val maximum = values.maxOfOrNull { it.sum() } ?: 0.0
    val step = when {
        maximum <= 50 -> 10.0
        maximum <= 100 -> 20.0
        maximum <= 250 -> 50.0
        else -> ceil(maximum / 500) * 100
    }
    val ceiling = (ceil(maximum / step) * step).coerceAtLeast(step * 5)
    val colors = listOf(ReferenceStyle.Protein, ReferenceStyle.Fat, ReferenceStyle.Carbs)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val grid = MaterialTheme.colorScheme.outlineVariant
    val measurer = rememberTextMeasurer()
    val labels = dates.map {
        val date = LocalDate.parse(it)
        if (weekly) "${date.dayOfWeek.getDisplayName(DateTextStyle.NARROW, Locale.getDefault())}${date.dayOfMonth}" else "Today".takeIf { date == LocalDate.now() } ?: date.format(DateTimeFormatter.ofPattern("MMM d"))
    } + if (weekly) listOf("AVG") else emptyList()
    val summary = values.mapIndexed { index, parts -> "${if (index == dates.size) "Daily average" else dates[index]}: protein ${parts[0].roundToInt()} grams, fat ${parts[1].roundToInt()} grams, carbohydrates ${parts[2].roundToInt()} grams" }.joinToString(". ")
    Column {
        Text("${if (weekly) "Daily macros" else "Macros"} (g)", Modifier.padding(start = 16.dp, top = 12.dp), color = muted, fontSize = 12.sp)
        Canvas(Modifier.fillMaxWidth().height(280.dp).padding(12.dp).semantics { contentDescription = summary }) {
            val textStyle = TextStyle(color = muted, fontSize = 10.sp)
            val axisWidth = measurer.measure(ceiling.roundToInt().toString(), textStyle).size.width + 10.dp.toPx()
            val top = 10.dp.toPx()
            val bottom = size.height - 24.dp.toPx()
            val plotHeight = bottom - top
            val plotWidth = size.width - axisWidth
            val ticks = (ceiling / step).roundToInt()
            repeat(ticks + 1) { tick ->
                val y = bottom - plotHeight * tick / ticks
                drawLine(grid, Offset(axisWidth, y), Offset(size.width, y), pathEffect = if (tick == 0) null else PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx())))
                val label = measurer.measure((tick * step).roundToInt().toString(), textStyle)
                drawText(label, topLeft = Offset(axisWidth - label.size.width - 6.dp.toPx(), y - label.size.height / 2))
            }
            val slot = plotWidth / values.size
            val width = (slot * .68f).coerceAtMost(48.dp.toPx())
            values.forEachIndexed { index, parts ->
                val center = axisWidth + slot * (index + .5f)
                var y = bottom
                parts.forEachIndexed { part, grams ->
                    val height = (grams.coerceAtLeast(0.0) / ceiling * plotHeight).toFloat()
                    y -= height
                    drawRect(colors[part], Offset(center - width / 2, y), Size(width, height))
                }
                val label = measurer.measure(labels[index], textStyle)
                drawText(label, topLeft = Offset(center - label.size.width / 2, bottom + 5.dp.toPx()))
            }
        }
        if (maximum == 0.0) Text("Log food to see your macros.", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = muted, fontSize = 13.sp)
    }
}

@Composable
private fun MacroLegend(entries: List<FoodEntry>, weekly: Boolean) {
    val grams = listOf(entries.sumOf { it.carbsGrams }, entries.sumOf { it.fatGrams }, entries.sumOf { it.proteinGrams })
    val energy = grams[0] * 4 + grams[1] * 9 + grams[2] * 4
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text("${if (weekly) "Daily average" else "Total"} (g)", Modifier.weight(1f), color = muted, fontSize = 12.sp)
        Text("% of calories", color = muted, fontSize = 12.sp)
    }
    grams.forEachIndexed { index, value ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(16.dp).background(listOf(ReferenceStyle.Carbs, ReferenceStyle.Fat, ReferenceStyle.Protein)[index]))
            Text(listOf("Carbohydrates", "Fat", "Protein")[index], Modifier.weight(1f), fontSize = 14.sp)
            Text("${(value / if (weekly) 7 else 1).roundToInt()}g", color = muted, fontSize = 14.sp)
            Text("${if (energy > 0) (value * (if (index == 1) 9 else 4) * 100 / energy).roundToInt() else 0}%", Modifier.widthIn(min = 44.dp), color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    }
}

@Composable
private fun NutritionPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), border = BorderStroke(.5.dp, MaterialTheme.colorScheme.outline), shadowElevation = 1.dp) {
        Column(content = content)
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(thickness = 7.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun NutritionLine(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = color)
    }
}
