package com.kcalfit.app.ui.nakafit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import kotlinx.coroutines.delay

 data class NakaWorkout(val id: String, val title: String, val category: String, val image: Int,
    val minutes: Int, val level: String, val exercises: List<String>)

val nakaWorkouts = listOf(
    NakaWorkout("strength", "Squat Low, Build Strength", "Strength", R.drawable.nakafit_strength, 24, "Beginner",
        listOf("Bodyweight squat", "Reverse lunge", "Glute bridge", "Standing calf raise")),
    NakaWorkout("run", "Morning Run", "Cardio", R.drawable.nakafit_runner, 30, "All levels",
        listOf("Warm-up walk", "Easy run", "Steady pace", "Cool-down walk")),
    NakaWorkout("yoga", "Mindful Movement", "Yoga", R.drawable.nakafit_meditation, 20, "Beginner",
        listOf("Breathing practice", "Gentle stretch", "Balance flow", "Rest and recover")),
    NakaWorkout("weights", "Lift Weights, Get Stronger", "Strength", R.drawable.nakafit_gym_sunset, 35, "Intermediate",
        listOf("Goblet squat", "Dumbbell row", "Shoulder press", "Romanian deadlift"))
)

fun workoutForId(id: String): NakaWorkout = nakaWorkouts.firstOrNull { it.id == id }
    ?: if (id.startsWith("activity_")) {
        val activity = id.removePrefix("activity_")
        NakaWorkout(id, activity, activity, 0, 20, "Your pace", listOf("Warm up", "Main activity", "Cool down"))
    } else nakaWorkouts.first()

@Composable
fun WorkoutDiscoveryScreen(onWorkout: (String) -> Unit, onLog: () -> Unit, onPlan: () -> Unit, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    NakaPage("Exercise", onBack, action = { IconButton(onLog) { Icon(Icons.Default.Add, "Log exercise") } }) {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search exercise") },
            leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Strength", "Cardio", "Yoga").forEach { item ->
                FilterChip(category == item, { category = item }, label = { Text(item, fontSize = 12.sp) })
            }
        }
        NakaPhotoCard("Your Training Plan", "Build a routine that fits your week", R.drawable.nakafit_gym_motivation, onPlan, height = 200)
        NakaSection("Popular Exercise")
        val results = nakaWorkouts.filter { (category == "All" || it.category == category) &&
            (query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true)) }
        if (results.isEmpty()) Text("No matching exercises. Try another search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        results.forEach { workout ->
            NakaPhotoCard(workout.title, "${workout.minutes} minutes · ${workout.level}", workout.image, { onWorkout(workout.id) })
        }
    }
}

@Composable
fun WorkoutDetailScreen(workoutId: String, onBack: () -> Unit, onStart: () -> Unit) {
    val workout = workoutForId(workoutId)
    var expanded by rememberSaveable { mutableStateOf(-1) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            NakaPhoto(workout.image, workout.title, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .6f)))))
            IconButton(onBack, Modifier.padding(16.dp).background(Color.White, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(workout.title, Modifier.align(Alignment.BottomStart).padding(20.dp), color = Color.White,
                fontSize = 29.sp, lineHeight = 34.sp)
        }
        NakaPage("Workout Details") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NakaStat("${workout.minutes} min", "Duration", Modifier.weight(1f), Naka.Lavender)
                NakaStat(workout.level, "Level", Modifier.weight(1f), Naka.Lime)
            }
            Text("A balanced session with warm-up, focused movement and recovery.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            NakaSection("Exercises")
            workout.exercises.forEachIndexed { index, name ->
                Surface(shape = RoundedCornerShape(18.dp), color = Naka.Pale) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(Modifier.clickable { expanded = if (expanded == index) -1 else index }, verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}", Modifier.width(28.dp), color = Naka.Violet)
                            Text(name, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                            Icon(if (expanded == index) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Exercise instructions")
                        }
                        if (expanded == index) Text("Move at a comfortable pace. Keep your form controlled and take a short rest between sets.",
                            Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
            NakaButton("Start Exercise", onStart)
        }
    }
}

@Composable
fun WorkoutSessionScreen(workoutId: String, onBack: () -> Unit, onComplete: (Int, Int) -> Unit) {
    val workout = workoutForId(workoutId)
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    var running by rememberSaveable { mutableStateOf(false) }
    var complete by rememberSaveable { mutableStateOf(false) }
    var exercise by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(running) { while (running) { delay(1000); seconds++ } }
    NakaPage("Start Exercise", onBack) {
        if (workout.image != 0) NakaPhoto(workout.image, workout.title, Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)))
        Text(workout.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 25.sp, lineHeight = 30.sp)
        Box(Modifier.fillMaxWidth().height(185.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { (seconds.toFloat() / (workout.minutes * 60)).coerceIn(0f, 1f) },
                Modifier.size(170.dp), color = Naka.Violet, trackColor = Naka.Lavender, strokeWidth = 12.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("%02d:%02d".format(seconds / 60, seconds % 60), fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(if (running) "Time moving" else "Ready when you are", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NakaStat("${exercise + 1}/${workout.exercises.size}", "Exercise", Modifier.weight(1f))
            NakaStat("${seconds / 12}", "Estimated kcal", Modifier.weight(1f))
        }
        NakaRow(workout.exercises[exercise], "Tap for the next exercise", {
            exercise = (exercise + 1) % workout.exercises.size
        })
        NakaButton(if (running) "Pause" else if (seconds == 0) "Start Now" else "Resume", { running = !running })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NakaButton("Reset", { running = false; seconds = 0; exercise = 0 }, Modifier.weight(1f), outlined = true)
            NakaButton("Finish", { running = false; complete = true }, Modifier.weight(1f), enabled = seconds > 0, dark = true)
        }
    }
    if (complete) AlertDialog(onDismissRequest = { complete = false },
        icon = { Icon(Icons.Default.CheckCircle, null, tint = Naka.Violet, modifier = Modifier.size(54.dp)) },
        title = { Text("Exercise Complete") },
        text = { Column { Text("Great work! You moved for ${seconds / 60} minutes and ${seconds % 60} seconds.");
            Text("${seconds / 12} estimated kcal · ${workout.title}", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        confirmButton = { TextButton({ complete = false; onComplete((seconds / 60).coerceAtLeast(1), seconds / 12) }) { Text("Save Workout") } },
        dismissButton = { TextButton({ complete = false }) { Text("Keep Going") } })
}
