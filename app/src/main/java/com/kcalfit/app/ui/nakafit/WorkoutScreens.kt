package com.kcalfit.app.ui.nakafit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.kcalfit.app.R
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.workout.TrainingPreferences
import com.kcalfit.app.domain.workout.DailyWorkout
import com.kcalfit.app.domain.workout.DailyWorkoutGenerator
import com.kcalfit.app.domain.workout.Equipment
import com.kcalfit.app.domain.workout.ExerciseLibrary
import com.kcalfit.app.domain.workout.Experience
import com.kcalfit.app.domain.workout.LibraryExercise
import com.kcalfit.app.domain.workout.PlannedMove
import com.kcalfit.app.domain.workout.WorkoutPhase
import com.kcalfit.app.ui.exercise.ExerciseDemoSheet
import com.kcalfit.app.ui.exercise.ExerciseDetailSheet
import java.time.LocalDate
import com.kcalfit.app.data.workout.WorkoutNotifications
import com.kcalfit.app.data.workout.WorkoutSessionTracker
import com.kcalfit.app.domain.activity.ActivityCatalog
import com.kcalfit.app.domain.workout.WorkoutSessionSpec
import com.kcalfit.app.domain.workout.WorkoutSessionState
import com.kcalfit.app.domain.workout.estimatedCalories
import com.kcalfit.app.domain.workout.formatElapsed
import kotlinx.coroutines.delay

/** [moves] holds the prescription for each exercise when the workout was planned for the user. */
data class NakaWorkout(val id: String, val title: String, val category: String, val image: Int,
    val minutes: Int, val level: String, val exercises: List<String>, val moves: List<PlannedMove> = emptyList())

const val TODAY_WORKOUT_ID = "plan_today"

private const val GENERIC_STEP_NOTE = "Move at a comfortable pace. Keep your form controlled and take a short rest between sets."

fun DailyWorkout.toNakaWorkout(level: Experience) = NakaWorkout(TODAY_WORKOUT_ID, title, focus.label,
    if (isRestDay) R.drawable.nakafit_meditation else R.drawable.nakafit_gym_motivation, estimatedMinutes, level.label,
    moves.map { it.exercise.name }, moves)

/** Today's plan from the profile and the choices made on the Exercise page; changing a choice regenerates it. */
class TodayWorkoutState(private val prefs: TrainingPreferences, private val profile: UserEntity?) {
    var inputs by mutableStateOf(prefs.inputs(profile)); private set
    val workout: DailyWorkout get() = DailyWorkoutGenerator.generate(inputs, LocalDate.now())
    val experience get() = inputs.experience ?: DailyWorkoutGenerator.DEFAULT_EXPERIENCE

    fun setMinutes(minutes: Int) { prefs.setSessionMinutes(minutes); inputs = prefs.inputs(profile) }
    fun setExperience(value: Experience) { prefs.setExperience(value); inputs = prefs.inputs(profile) }
    fun setEquipment(value: Equipment) { prefs.setEquipment(value); inputs = prefs.inputs(profile) }
}

@Composable
fun rememberTodayWorkout(profile: UserEntity?): TodayWorkoutState {
    val context = LocalContext.current
    return remember(profile) { TodayWorkoutState(TrainingPreferences(context), profile) }
}

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
        val key = id.removePrefix("activity_")
        val activity = ActivityCatalog.byId(key)
        NakaWorkout(id, activity?.name ?: key, activity?.category?.label ?: key, 0, 20, "Your pace",
            listOf("Warm up", "Main activity", "Cool down"))
    } else nakaWorkouts.first()

/** Steps are offered only for on-foot activities, and appear only when the phone is counting them. */
fun NakaWorkout.sessionSpec() = WorkoutSessionSpec(id, title, category, minutes, exercises,
    tracksSteps = id == "run" || ActivityCatalog.byId(id.removePrefix("activity_"))?.tracksSteps == true,
    exerciseNotes = moves.map { "${it.phase.label} · ${it.prescription}" })

@Composable
fun WorkoutDiscoveryScreen(onWorkout: (String) -> Unit, onLog: () -> Unit, onPlan: () -> Unit, onBack: () -> Unit,
    profile: UserEntity? = null) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    val today = rememberTodayWorkout(profile).workout
    NakaPage("Exercise", onBack, action = { IconButton(onLog) { Icon(Icons.Default.Add, "Log exercise") } }) {
        NakaSection("Today's Workout")
        Surface(Modifier.fillMaxWidth().clickable { onWorkout(TODAY_WORKOUT_ID) }, shape = RoundedCornerShape(22.dp), color = Naka.Pale) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(today.title, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                val main = today.phase(WorkoutPhase.MAIN).size
                Text(if (today.isRestDay) "Rest and stretch · about ${today.estimatedMinutes} min"
                    else "About ${today.estimatedMinutes} min · $main exercises", color = Naka.Violet, fontSize = 14.sp)
                Text(today.basis.first(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text(if (today.isRestDay) "See today's stretches" else "See exercises and start", color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        // Activity tracking (walk, run, ride…) stays separate from the planned workout.
        NakaRow("Track an activity", "Walking, running, cycling and more, with a live timer", onLog)
        NakaSection("Browse Workouts")
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
fun WorkoutDetailScreen(workoutId: String, onBack: () -> Unit, onStart: () -> Unit, profile: UserEntity? = null) {
    val todayState = if (workoutId == TODAY_WORKOUT_ID) rememberTodayWorkout(profile) else null
    val today = todayState?.workout
    val workout = today?.toNakaWorkout(todayState.experience) ?: workoutForId(workoutId)
    var demoName by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<Pair<LibraryExercise, PlannedMove?>?>(null) }
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
                NakaStat("${workout.minutes} min", if (today != null) "Estimated" else "Duration", Modifier.weight(1f), Naka.Lavender)
                NakaStat(workout.level, "Level", Modifier.weight(1f), Naka.Lime)
            }
            if (today != null && todayState != null) {
                TodayPlanOptions(todayState, today)
                if (today.isRestDay) Text("Today is a rest day in your ${todayState.inputs.daysPerWeek ?: DailyWorkoutGenerator.DEFAULT_DAYS}-day week. " +
                    "Gentle stretching helps recovery; to record a walk, use Track an activity on the Exercise page.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                WorkoutPhase.entries.forEach { phase ->
                    val moves = today.phase(phase)
                    if (moves.isNotEmpty()) {
                        NakaSection(phase.label)
                        moves.forEach { move -> PlannedMoveRow(move) { detail = move.exercise to move } }
                    }
                }
                Text("Tap an exercise for instructions. Move on when you've finished it; the timer tracks the whole session, not individual sets.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            } else {
                Text("A balanced session with warm-up, focused movement and recovery.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                NakaSection("Exercises")
                workout.exercises.forEachIndexed { index, name ->
                    val known = ExerciseLibrary.find(name)
                    Surface(shape = RoundedCornerShape(18.dp), color = Naka.Pale) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(Modifier.clickable { if (known != null) detail = known to null else demoName = name },
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", Modifier.width(28.dp), color = Naka.Violet)
                                Text(name, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Default.ChevronRight, "Exercise details")
                            }
                        }
                    }
                }
            }
            NakaButton(if (today?.isRestDay == true) "Start Stretching" else "Start Exercise", onStart)
        }
    }
    detail?.let { (exercise, move) -> ExerciseDetailSheet(exercise, move) { detail = null } }
    demoName?.let { ExerciseDemoSheet(it, listOf(GENERIC_STEP_NOTE)) { demoName = null } }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodayPlanOptions(state: TodayWorkoutState, today: DailyWorkout) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        today.basis.take(2).forEach { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
        Text("Session length", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DailyWorkoutGenerator.MINUTE_OPTIONS.forEach { minutes ->
                FilterChip(state.inputs.sessionMinutes == minutes, { state.setMinutes(minutes) }, label = { Text("$minutes min") })
            }
        }
        Text("Experience", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Experience.entries.forEach { level ->
                FilterChip(state.inputs.experience == level, { state.setExperience(level) }, label = { Text(level.label) })
            }
        }
        Text("Equipment", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Equipment.entries.forEach { kit ->
                FilterChip(state.inputs.equipment == kit, { state.setEquipment(kit) }, label = { Text(kit.label) })
            }
        }
        // Lists any choice still on its default, so nothing unpicked looks like the user's setting.
        today.basis.drop(2).filter { "default" in it }.takeIf { it.isNotEmpty() }?.let { defaults ->
            Text("Using defaults: " + defaults.joinToString { it.substringBefore(" (") }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlannedMoveRow(move: PlannedMove, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), color = Naka.Pale) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(move.exercise.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text(move.prescription, color = Naka.Violet, fontSize = 13.sp)
                val extra = listOfNotNull(move.restText, move.exercise.muscles.joinToString { it.label })
                Text(extra.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, "Details for ${move.exercise.name}")
        }
    }
}

@Composable
fun WorkoutSessionScreen(workoutId: String, onBack: () -> Unit, onOpenSession: (String) -> Unit = {},
    autoStart: Boolean = false, planned: NakaWorkout? = null, onComplete: (Int, Int) -> Unit) {
    val workout = planned ?: workoutForId(workoutId)
    val context = LocalContext.current
    val session by WorkoutSessionTracker.state.collectAsState()
    val active = session as? WorkoutSessionState.Active
    val mine = active?.takeIf { it.spec.workoutId == workout.id }
    val other = active?.takeIf { it.spec.workoutId != workout.id }
    val running = mine?.paused == false
    var complete by rememberSaveable { mutableStateOf(false) }
    // Only redraws the clock; elapsed time itself comes from the tracker. [now] must be read here,
    // during composition, or ticking it never recomposes the screen.
    var now by remember { mutableLongStateOf(WorkoutSessionTracker.now()) }
    LaunchedEffect(running, session) { now = WorkoutSessionTracker.now(); while (running) { delay(1000); now = WorkoutSessionTracker.now() } }
    LaunchedEffect(mine == null) { if (mine == null) complete = false }
    val metrics = if (mine != null) WorkoutSessionTracker.metrics(context, now) else null
    val seconds = metrics?.elapsedSeconds ?: 0
    // A running session keeps the exercises it started with, even if the plan's options change.
    val spec = mine?.spec ?: workout.sessionSpec()
    val exercise = (mine?.exerciseIndex ?: 0).coerceIn(0, (spec.exercises.size - 1).coerceAtLeast(0))
    val currentName = spec.exercises.getOrNull(exercise)
    val currentMove = workout.moves.getOrNull(exercise)?.takeIf { it.exercise.name == currentName }
    val currentInfo = currentMove?.exercise ?: currentName?.let(ExerciseLibrary::find)
    var showInfo by remember { mutableStateOf(false) }
    // Planned workouts get a demo for every step; activity sessions (walk, ride…) have no exercises to show.
    val demoName = currentName?.takeIf { currentInfo == null && !workout.id.startsWith("activity_") }
    // Newly granted access lets the already-running session go foreground and post its notification.
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        WorkoutSessionTracker.refreshService(context)
    }
    /** The session runs from the tap; permission prompts follow and never hold the timer back. */
    fun requestAndStart() {
        WorkoutSessionTracker.start(context, workout.sessionSpec())
        fun missing(permission: String) = ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        val needed = buildList {
            if (Build.VERSION.SDK_INT >= 33 && missing(Manifest.permission.POST_NOTIFICATIONS)) add(Manifest.permission.POST_NOTIFICATIONS)
            // Android 14+ only keeps a workout running in the background with physical-activity access.
            if (Build.VERSION.SDK_INT >= 34 && missing(Manifest.permission.ACTIVITY_RECOGNITION)) add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (needed.isNotEmpty()) permissions.launch(needed.toTypedArray())
    }
    // Arriving from a Start button begins the session at once, but only on first entry, so returning
    // to this screen after the workout ended never starts another one.
    var autoStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (autoStart && !autoStarted) {
            autoStarted = true
            if (WorkoutSessionTracker.state.value !is WorkoutSessionState.Active) requestAndStart()
        }
    }
    NakaPage("Start Exercise", onBack) {
        if (workout.image != 0) NakaPhoto(workout.image, workout.title, Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)))
        Text(workout.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 25.sp, lineHeight = 30.sp)
        if (other != null) Surface(shape = RoundedCornerShape(18.dp), color = Naka.Pale) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${other.spec.title} is in progress. End it before starting another workout.",
                    color = MaterialTheme.colorScheme.onSurface)
                NakaButton("Open current workout", { onOpenSession(other.spec.workoutId) }, outlined = true)
            }
        }
        Box(Modifier.fillMaxWidth().height(185.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { (seconds.toFloat() / (spec.plannedMinutes.coerceAtLeast(1) * 60)).coerceIn(0f, 1f) },
                Modifier.size(170.dp), color = Naka.Violet, trackColor = Naka.Lavender, strokeWidth = 12.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatElapsed(seconds), fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(if (running) "Time moving" else if (mine != null) "Paused" else "Ready when you are", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NakaStat("${exercise + 1}/${spec.exercises.size}", "Exercise", Modifier.weight(1f))
            NakaStat("${estimatedCalories(seconds)}", "Estimated kcal", Modifier.weight(1f))
        }
        metrics?.steps?.let { steps -> NakaStat("$steps", "Phone steps this session", Modifier.fillMaxWidth()) }
        if (currentName != null) {
            val hint = if (mine != null) "Tap for the next exercise" else "Your first exercise"
            NakaRow(currentName, listOfNotNull(spec.exerciseNotes.getOrNull(exercise), hint).joinToString(" · "), {
                if (mine != null) WorkoutSessionTracker.nextExercise()
            })
            if (currentInfo != null || demoName != null) TextButton({ showInfo = true }) { Text("How to do this exercise") }
        }
        NakaButton(if (running) "Pause" else if (mine == null) "Start Now" else "Resume", {
            when {
                mine == null -> requestAndStart()
                running -> WorkoutSessionTracker.pause(context)
                else -> WorkoutSessionTracker.resume(context)
            }
        }, enabled = other == null)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NakaButton("Reset", { if (mine != null) WorkoutSessionTracker.discard() }, Modifier.weight(1f), outlined = true)
            NakaButton("Finish", { WorkoutSessionTracker.pause(context); complete = true }, Modifier.weight(1f), enabled = mine != null, dark = true)
        }
        if (mine != null) {
            if (!WorkoutNotifications.notificationsEnabled(context))
                Text("Notifications are off for kcalfit, so this workout won't appear in your notification shade or lock screen.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            if (!WorkoutNotifications.canRunInForeground(context))
                Text("Physical activity access is off, so Android may stop this workout while kcalfit is in the background. Keep the app open, or allow access in Settings.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
    if (complete) AlertDialog(onDismissRequest = { complete = false },
        icon = { Icon(Icons.Default.CheckCircle, null, tint = Naka.Violet, modifier = Modifier.size(54.dp)) },
        title = { Text(if (seconds > 0) "Exercise Complete" else "End Workout?") },
        text = { Column {
            if (seconds > 0) {
                Text("Great work! You moved for ${seconds / 60} minutes and ${seconds % 60} seconds.")
                Text("${estimatedCalories(seconds)} estimated kcal · ${workout.title}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else Text("No time was recorded, so nothing will be saved to your log.")
        } },
        confirmButton = { TextButton({
            complete = false
            // Null when the notification's Finish already ended this session.
            WorkoutSessionTracker.end(context)?.takeIf { it.worthSaving }?.let { onComplete(it.minutes, it.calories) }
        }) { Text(if (seconds > 0) "Save Workout" else "End Workout") } },
        dismissButton = { TextButton({ complete = false }) { Text("Keep Going") } })
    if (showInfo) when {
        currentInfo != null -> ExerciseDetailSheet(currentInfo, currentMove) { showInfo = false }
        demoName != null -> ExerciseDemoSheet(demoName, listOf(GENERIC_STEP_NOTE)) { showInfo = false }
    }
}
