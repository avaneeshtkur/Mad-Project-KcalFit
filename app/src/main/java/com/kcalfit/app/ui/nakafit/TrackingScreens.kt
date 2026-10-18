package com.kcalfit.app.ui.nakafit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kcalfit.app.R
import com.kcalfit.app.data.preferences.ActivityGoalStore
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.time.LocalDate

@Composable
fun TrackScreen(uiState: CalFitUiState, userId: String, onGoal: (String, String) -> Unit,
    onAddGoal: () -> Unit, onProgress: () -> Unit, onHealth: () -> Unit, onSleep: () -> Unit,
    onPeriod: () -> Unit, onWater: () -> Unit, onMeal: (MealType) -> Unit,
    onAddFood: (MealType) -> Unit, onFood: (FoodEntry) -> Unit, onDelete: (FoodEntry) -> Unit) {
    val context = LocalContext.current
    val goals = ActivityGoalStore(context).get(userId)
    NakaPage("Track", action = { IconButton(onAddGoal) { Icon(Icons.Default.Add, "Add goal") } }) {
        TrackActivityOverview(uiState)
        CalorieHistory(uiState)
        MealDiarySection(uiState, onMeal, onAddFood, onFood, onDelete)
        NakaSection("Daily Goals", "Add Goals", onAddGoal)
        if (goals.isEmpty()) NakaRow("Morning Run", "Create your first activity goal", onAddGoal,
            leading = { Icon(Icons.Default.DirectionsRun, null, tint = Naka.Violet) })
        else goals.forEach { goal -> NakaRow(goal.title, "${goal.activity} · ${goal.repeat} · ${goal.reminder}", { onGoal(goal.title,goal.activity) }) }
        NakaPhotoCard("Your Progress", "Small steps, lasting change", R.drawable.kcalfit_track_running, onProgress)
        NakaSection("Health Metrics")
        NakaRow("Health Metrics Summary", "Calories, weight and activity", onHealth, { Icon(Icons.Default.MonitorHeart, null, tint = Naka.Violet) })
        NakaRow("Sleep Quality", "Log your rest and recovery", onSleep, { Icon(Icons.Default.Bedtime, null, tint = Naka.Violet) })
        NakaRow("Period", "Your cycle calendar", onPeriod, { Icon(Icons.Default.CalendarMonth, null, tint = Naka.Violet) })
        NakaRow("Water Intake", "${uiState.totalWaterMl} ml today", onWater, { Icon(Icons.Default.WaterDrop, null, tint = Naka.Violet) })
    }
}

@Composable
private fun TrackActivityOverview(uiState: CalFitUiState) {
    val healthContext = androidx.compose.ui.platform.LocalContext.current
    val tracker = remember { com.kcalfit.app.data.health.StepTracking(healthContext) }
    var steps by remember(uiState.selectedDate) { mutableStateOf(tracker.count(uiState.selectedDate)) }
    var healthDistance by remember(uiState.selectedDate) { mutableStateOf(tracker.distance(uiState.selectedDate)) }
    DisposableEffect(tracker, uiState.selectedDate) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> steps = tracker.count(uiState.selectedDate); healthDistance = tracker.distance(uiState.selectedDate) }
        tracker.prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { tracker.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val selected = runCatching { LocalDate.parse(uiState.selectedDate) }.getOrDefault(LocalDate.now())
    val dates = com.kcalfit.app.domain.calculator.FoodDiaryRules.week(selected)
    val dateKeys = dates.map(LocalDate::toString).toSet()
    val entries = uiState.allExerciseEntries.filter { it.date in dateKeys }
    val durations = dates.map { date -> entries.filter { it.date == date.toString() }.sumOf { it.durationMinutes.coerceAtLeast(0) }.toFloat() }
    val todayDistances = uiState.exerciseEntries.map { it.distanceKm }.filter { it.isFinite() && it > 0.0 }
    val distanceToday = todayDistances.sum()
    val hasDistanceToday = todayDistances.isNotEmpty()
    val validDistances = entries.map { it.distanceKm }.filter { it.isFinite() && it > 0.0 }
    val hasDistanceHistory = validDistances.isNotEmpty()
    val maxMinutes = durations.maxOrNull() ?: 0f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DirectionsRun, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Activity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("${dates.first().format(DateTimeFormatter.ofPattern("d MMM"))} – ${dates.last().format(DateTimeFormatter.ofPattern("d MMM"))}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (maxMinutes > 0f) "${maxMinutes.toInt()} min peak" else "This week",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TrackMetric(
                    label = "Steps",
                    value = steps?.toString() ?: "—",
                    supporting = if (steps != null) "steps · ${tracker.prefs.getString("source", "")}" else "Connect a step source in More",
                    icon = Icons.Default.DirectionsWalk,
                    modifier = Modifier.weight(1f)
                )
                TrackMetric(
                    label = "Distance",
                    value = if (healthDistance != null) String.format(Locale.getDefault(), "%.2f", healthDistance!! / 1000) else if (hasDistanceToday) String.format(Locale.getDefault(), "%.2f", distanceToday) else "—",
                    supporting = if (healthDistance != null) "km · Health Connect" else if (hasDistanceToday) "km today · logged workouts" else "No distance logged today",
                    icon = Icons.Default.Route,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Logged activity", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("minutes per day", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TrackWeekChart(dates.map { it.format(DateTimeFormatter.ofPattern("EE")) }, durations, dates.indexOf(selected))
            }
            if (hasDistanceHistory) {
                val km = validDistances.sum()
                Text("${String.format(Locale.getDefault(), "%.2f", km)} km recorded this week",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Distance appears when it is included in a saved workout. No historical distance is available yet.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TrackMetric(
    label: String,
    value: String,
    supporting: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(supporting, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, minLines = 2)
            }
        }
    }
}

@Composable
private fun TrackWeekChart(dayLabels: List<String>, minutes: List<Float>, selectedIndex: Int) {
    val grid = MaterialTheme.colorScheme.outlineVariant
    val bar = MaterialTheme.colorScheme.primary
    val label = MaterialTheme.colorScheme.onSurfaceVariant
    val peak = minutes.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val hasData = minutes.any { it > 0f }
    Column(Modifier.fillMaxWidth().semantics {
        contentDescription = if (hasData) dayLabels.zip(minutes).joinToString { (day, value) -> "$day: ${value.toInt()} minutes" }
            else "No recorded workout minutes this week"
    }) {
        Box {
            Canvas(Modifier.fillMaxWidth().height(94.dp).padding(horizontal = 5.dp)) {
                (0..3).forEach { index ->
                    val y = size.height * index / 3f
                    drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }
                val slot = size.width / minutes.size.coerceAtLeast(1)
                minutes.forEachIndexed { index, value ->
                    if (value > 0f) {
                        val height = (value / peak * size.height).coerceAtLeast(3.dp.toPx())
                        drawRoundRect(bar.copy(alpha = if (index == minutes.lastIndex) 1f else .72f),
                            Offset(index * slot + slot * .22f, size.height - height),
                            Size(slot * .56f, height), CornerRadius(8.dp.toPx(), 8.dp.toPx()))
                    }
                }
            }
            if (!hasData) Text("No workouts logged this week", Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayLabels.forEachIndexed { index, day ->
                Text(day, color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else label,
                    style = MaterialTheme.typography.labelSmall, fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun DailyGoalScreen(title: String, activity: String, userId: String, onBack: () -> Unit, onStart: () -> Unit,
    uiState: CalFitUiState, onMeal: (MealType) -> Unit, onAddFood: (MealType) -> Unit,
    onFood: (FoodEntry) -> Unit, onDelete: (FoodEntry) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nakafit_goal_completion", 0) }
    val key = "${userId}_${activity}_${title}_${uiState.selectedDate}"
    val goal = ActivityGoalStore(context).get(userId).firstOrNull { it.title == title && it.activity == activity }
    var completed by remember(key) { mutableStateOf(prefs.getBoolean(key,false)) }
    val image = if (activity == "Yoga") R.drawable.nakafit_meditation else if (activity == "Weights") R.drawable.nakafit_strength else R.drawable.nakafit_runner
    NakaPage("Daily Goals", onBack) {
        NakaPhoto(image, activity, Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp)))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp)
        Text("$activity · Keep a comfortable pace and focus on your goal.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NakaStat(activity, "Activity", Modifier.weight(1f), Naka.Lavender)
            NakaStat(goal?.repeat ?: "Daily", "Repeat", Modifier.weight(1f), Naka.Lime)
        }
        Text("Reminder: ${goal?.reminder ?: "06:00"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(completed, { completed = it; prefs.edit().putBoolean(key,it).apply() })
            Text(if (completed) "Goal completed today" else "Mark as completed", color = MaterialTheme.colorScheme.onSurface)
        }
        NakaButton("Start Exercise", onStart)
        CalorieHistory(uiState)
        MealDiarySection(uiState, onMeal, onAddFood, onFood, onDelete)
    }
}

@Composable
fun ProgressScreen(uiState: CalFitUiState, onBack: () -> Unit,
    onMeasurement: (Double?, Double?) -> Unit, onLog: () -> Unit) {
    var entry by remember { mutableStateOf<String?>(null) }
    var height by remember(uiState.userProfile?.heightCm) { mutableFloatStateOf((uiState.userProfile?.heightCm ?: 170.0).toFloat()) }
    var weight by remember(uiState.latestWeightKg) { mutableFloatStateOf(uiState.latestWeightKg.toFloat()) }
    val week = com.kcalfit.app.domain.calculator.FoodDiaryRules.week(LocalDate.parse(uiState.selectedDate)).map { it.toString() }
    val activeDays = uiState.allExerciseEntries.filter { it.date in week }.map { it.date }.distinct().size
    val targetDays = (uiState.userProfile?.exerciseDaysPerWeek ?: 3).coerceAtLeast(1)
    val progress = (activeDays.toFloat() / targetDays).coerceIn(0f,1f)
    NakaPage("Progress", onBack) {
        Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, Modifier.size(235.dp), color = Naka.Violet, trackColor = Naka.Pale, strokeWidth = 20.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(progress * 100).toInt()}%", fontSize = 42.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("$activeDays / $targetDays active days this week", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NakaStat("${weight.toInt()} kg", "Weight", Modifier.weight(1f), Naka.Lavender)
            NakaStat("${height.toInt()} cm", "Height", Modifier.weight(1f), Naka.Lime)
        }
        NakaRow("Entry Height", "${height.toInt()} centimeters", { entry = "Height" })
        NakaRow("Entry Weight", "${"%.1f".format(weight)} kilograms", { entry = "Weight" })
        NakaSection("Health Log")
        NakaRow("${"%.1f".format(weight)} kg", uiState.selectedDate, onLog)
        NakaButton("Entry", { entry = "Weight" })
    }
    if (entry != null) MeasurementDialog(entry!!, if (entry == "Height") height else weight,
        { entry = null }, { value ->
            if (entry == "Height") { height = value; onMeasurement(value.toDouble(), null) }
            else { weight = value; onMeasurement(null, value.toDouble()) }
            entry = null
        })
}

@Composable
fun MeasurementDialog(kind: String, initial: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    var value by remember { mutableFloatStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(25.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("What Is Your $kind", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Log and monitor daily health data", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(if (kind == "Height") "${value.toInt()}" else "%.1f".format(value), fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurface)
                Slider(value, { value = it }, valueRange = if (kind == "Height") 100f..230f else 30f..200f)
                Surface(shape = RoundedCornerShape(18.dp), color = Naka.Lavender) {
                    Text(if (kind == "Height") "centimeters" else "kilograms", Modifier.padding(10.dp,5.dp), color = Naka.Violet, fontSize = 11.sp)
                }
                NakaButton("Select", { onSelect(value) })
            }
        }
    }
}

@Composable
fun HealthSummaryScreen(uiState: CalFitUiState, onBack: () -> Unit, onLog: () -> Unit) {
    NakaPage("Health Metrics Summary", onBack) {
        Text(uiState.selectedDate, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NakaStat("${uiState.caloriesBurned}", "Calories burnt", Modifier.weight(1f), Naka.Lavender)
            NakaStat("—", "Distance covered", Modifier.weight(1f), Naka.Lime)
        }
        NakaSection("Today’s Exercise")
        ActivityBars(uiState.exerciseEntries.map { it.durationMinutes.toFloat() }.ifEmpty { listOf(0f) }, "min")
        NakaRow("Weight", "${uiState.latestWeightKg} kg", onLog)
        NakaRow("Height", "${uiState.userProfile?.heightCm ?: 170} cm", onLog)
        NakaRow("Daily calorie intake", "${uiState.totalCalories} kcal", onLog)
        NakaRow("Water", "${uiState.totalWaterMl} ml", onLog)
        NakaButton("Daily Health Log Entry", onLog)
    }
}

@Composable
fun HealthLogScreen(onBack: () -> Unit, onWeight: (Double) -> Unit, onWater: (Int) -> Unit) {
    var selected by rememberSaveable { mutableStateOf("Weight") }
    var value by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    NakaPage("Daily Health Log Entry", onBack) {
        Text("What would you like to log?", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Weight", "Water").forEach { item -> FilterChip(selected == item, { selected = item; value = "" }, label = { Text(item) }) }
        }
        OutlinedTextField(value, { value = it }, Modifier.fillMaxWidth(), label = { Text(if (selected == "Weight") "Weight in kg" else "Water in ml") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), shape = RoundedCornerShape(24.dp))
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        NakaButton("Save Entry", {
            val amount = value.toDoubleOrNull()
            if (amount == null || amount <= 0 || (selected == "Weight" && amount !in 30.0..300.0) || (selected == "Water" && amount > 5000)) error = "Enter a valid amount"
            else { if (selected == "Weight") onWeight(amount) else onWater(amount.toInt()); onBack() }
        })
    }
}

@Composable
fun InsightScreen(uiState: CalFitUiState, onGoals: () -> Unit, onProgress: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf("All") }
    var period by rememberSaveable { mutableStateOf("Week") }
    NakaPage("Insight") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("All", "Daily Goals").forEach { item -> FilterChip(tab == item, { tab = item }, label = { Text(item) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Day", "Week", "Month").forEach { item -> FilterChip(period == item, { period = item }, label = { Text(item) }) }
        }
        NakaSection(if (tab == "All") "Activity Overview" else "Goal Progress")
        val days = if (period == "Day") 1 else if (period == "Week") 7 else 30
        val bars = (0 until days).map { index ->
            uiState.allEntries.filter { it.date == LocalDate.parse(uiState.selectedDate).minusDays((days-1-index).toLong()).toString() }.sumOf { it.calories }.toFloat()
        }
        Surface(color = Naka.Pale, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Calories logged", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
                ActivityBars(bars)
                Text("From your food diary", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NakaStat("${(uiState.totalCalories.toFloat()/uiState.dailyGoal.coerceAtLeast(1)*100).toInt()}%", "Daily calorie goal", Modifier.weight(1f), Naka.Lavender)
            NakaStat("${uiState.userProfile?.currentStreakDays ?: 0} days", "Current streak", Modifier.weight(1f), Naka.Lime)
        }
        NakaRow("Daily Goals", "Review your goals and reminders", onGoals)
        NakaRow("Progress", "Your measurements and workout goal", onProgress)
    }
}

@Composable
fun CalorieHistory(uiState: CalFitUiState) {
    val end = LocalDate.parse(uiState.selectedDate)
    val dates = (6L downTo 0L).map { end.minusDays(it) }
    NakaSection("Calories Log")
    Text("${dates.first()} – ${dates.last()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    ActivityBars(dates.map { day -> uiState.allEntries.filter { it.date == day.toString() }.sumOf { it.calories }.toFloat() })
}

@Composable
fun ActivityBars(values: List<Float>, unit: String = "kcal") {
    val series = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val maximum = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    if (values.all { it <= 0 }) {
        Text("No records in this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("0", style = MaterialTheme.typography.labelSmall)
        Text("Peak: ${maximum.toInt()} $unit", style = MaterialTheme.typography.labelSmall)
    }
    Canvas(Modifier.fillMaxWidth().height(160.dp).padding(top = 12.dp, bottom = 12.dp)) {
        (0..4).forEach { i ->
            val y = size.height * i / 4f
            drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        val unit = size.width / values.size.coerceAtLeast(1)
        values.forEachIndexed { index, value ->
            if (value > 0) {
                val height = (value / maximum) * size.height
                drawRoundRect(series.copy(alpha = if (index == values.lastIndex) 1f else .75f),
                    Offset(index * unit + unit * .18f, size.height - height), Size(unit * .64f, height), CornerRadius(6f,6f))
            }
        }
    }
}

@Composable
fun SleepScreen(onBack: () -> Unit) {
    var hours by rememberSaveable { mutableFloatStateOf(7.5f) }
    var saved by rememberSaveable { mutableStateOf(false) }
    NakaPage("Sleep Quality", onBack) {
        NakaPhoto(R.drawable.nakafit_meditation, "Peaceful lakeside recovery", Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(24.dp)))
        Text("Rest and Recover", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp)
        NakaStat("${"%.1f".format(hours)} hours", "Sleep duration", Modifier.fillMaxWidth(), Naka.Lavender)
        Slider(hours, { hours = it; saved = false }, valueRange = 0f..12f, steps = 23)
        NakaSection("Your Sleep Log")
        Text(if (saved) "Today's sleep entry has been saved in this session." else "Choose how long you slept last night.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        NakaButton(if (saved) "Saved" else "Log Sleep", { saved = true })
    }
}

@Composable
fun PeriodScreen(onBack: () -> Unit) {
    var day by rememberSaveable { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    var logged by rememberSaveable { mutableStateOf(false) }
    NakaPage("Period", onBack) {
        Text("Your Cycle Calendar", color = MaterialTheme.colorScheme.onSurface, fontSize = 27.sp)
        Text(LocalDate.now().month.name.lowercase().replaceFirstChar(Char::uppercase), color = MaterialTheme.colorScheme.onSurfaceVariant)
        (1..LocalDate.now().lengthOfMonth()).toList().chunked(7).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                row.forEach { date ->
                    Surface(Modifier.weight(1f).height(43.dp).clickable { day = date; logged = false }, shape = CircleShape,
                        color = if (day == date) Naka.Violet else Naka.Pale) {
                        Box(contentAlignment = Alignment.Center) { Text("$date", color = if (day == date) Color.White else Naka.Ink) }
                    }
                }
                repeat(7-row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text("Selected day: $day", color = MaterialTheme.colorScheme.onSurfaceVariant)
        NakaButton(if (logged) "Entry Recorded" else "Log Period Start", { logged = true })
        Text("Entries in this preview are kept for this screen session.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}
