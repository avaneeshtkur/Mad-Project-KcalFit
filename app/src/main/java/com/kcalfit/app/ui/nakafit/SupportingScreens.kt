package com.kcalfit.app.ui.nakafit

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.ui.navigation.Screen
import com.kcalfit.app.ui.viewmodel.CalFitUiState

object NakaRoutes {
    const val WorkoutDetail = "workout_detail/{id}"
    /** `start=true` when a Start button opened it, so the session begins on arrival. */
    const val WorkoutSession = "workout_session/{id}?start={start}"
    const val ExerciseLog = "exercise_log"
    const val DailyGoal = "daily_goal/{title}/{activity}"
    const val Progress = "progress"
    const val HealthSummary = "health_summary"
    const val HealthLog = "health_log"
    const val Sleep = "sleep"
    const val Period = "period"
    const val Coaches = "coaches"
    const val CoachProfile = "coach_profile/{id}"
    const val ProfileInfo = "profile_info"
    const val Notifications = "notifications"
    const val Devices = "devices"
    const val Premium = "premium"
    const val Subscription = "subscription"
    const val Settings = "settings"
    const val Support = "support"
    const val Breakfast = "breakfast"
    const val Suggestions = "suggestions"
    const val Search = "search"
}

data class NakaCoach(val name: String, val age: Int, val specialty: String, val schedule: String,
    val summary: String, val photo: Int)
val nakaCoaches = listOf(
    NakaCoach("Sireen Sanchez",32,"Yoga & Mindfulness Sessions","4 times/week",
        "Sireen is a certified yoga instructor focused on flexibility and mindfulness. She combines yoga and meditation techniques.", R.drawable.nakafit_meditation),
    NakaCoach("Joshua Thompson",34,"Weight Loss Workouts","5 times/week",
        "Joshua is a personal trainer specializing in weight loss and body transformation. He creates personalized workout plans.", R.drawable.nakafit_strength),
    NakaCoach("Leona Roberts",28,"Dance Fitness Sessions","4 times/week",
        "Leona is a dance fitness instructor who integrates various dance styles into her workouts. Her sessions are fun and energetic.", R.drawable.nakafit_gym_sunset),
    NakaCoach("Jeremy Miller",29,"Running & Endurance","3 times/week",
        "Jeremy is an endurance coach with a background in marathon running. His workouts focus on building a steady pace.", R.drawable.nakafit_runner)
)

@Composable
fun CoachRecommendationScreen(onBack: () -> Unit, onCoach: (Int) -> Unit) {
    NakaPage("Coach Recommendation", onBack) {
        nakaCoaches.forEachIndexed { index, coach ->
            Surface(Modifier.fillMaxWidth().clickable { onCoach(index) }, shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Naka.Line)) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(54.dp), shape = CircleShape, color = Naka.Lavender) {
                            Box(contentAlignment = Alignment.Center) { Text(coach.name.split(' ').map { it.first() }.joinToString(""), color = Naka.Violet) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column { Text(coach.name, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface); Text("${coach.age} years old", fontSize = 13.sp) }
                    }
                    Text(coach.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Naka.Lavender, modifier = Modifier.weight(1.6f)) {
                            Text(coach.specialty, Modifier.padding(10.dp,8.dp), fontSize = 10.sp, maxLines = 1)
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = Naka.Lime, modifier = Modifier.weight(1f)) {
                            Text(coach.schedule, Modifier.padding(10.dp,8.dp), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Text("Coach profiles are demonstration content.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
fun CoachProfileScreen(index: Int, onBack: () -> Unit, onWorkout: (String) -> Unit) {
    val coach = nakaCoaches.getOrElse(index) { nakaCoaches.first() }
    var follow by rememberSaveable { mutableStateOf(false) }
    NakaPage("Profile Coach", onBack) {
        NakaPhoto(coach.photo, coach.specialty, Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)))
        Text(coach.name, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("${coach.specialty} · ${coach.age} years old", color = Naka.Violet, fontSize = 13.sp)
        Text(coach.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NakaStat("${index+4} years", "Experience", Modifier.weight(1f), Naka.Lavender)
            NakaStat(coach.schedule.substringBefore(' '), "Schedule", Modifier.weight(1f), Naka.Lime)
        }
        NakaButton(if (follow) "Following" else "Follow Coach", { follow = !follow })
        NakaSection("Recommended Session")
        val id = if (index == 0) "yoga" else if (index == 3) "run" else "strength"
        NakaPhotoCard(nakaWorkouts.first { it.id == id }.title, "View workout", coach.photo, { onWorkout(id) })
        Text("Demonstration profile. No booking or messaging service is connected.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
fun KcalFitProfileScreen(uiState: CalFitUiState, navigate: (String) -> Unit, onLogout: () -> Unit) {
    NakaPage("More") {
        NakaRow("Track", "Activity, steps and daily goals", { navigate(Screen.Track.route) })
        NakaRow("Nutrition", "Calories, nutrients and macros", { navigate(Screen.Nutrition.route) })
        NakaRow("Insight", "Your progress over time", { navigate(Screen.Insight.route) })
        NakaRow("Automatic steps", "Connect Health Connect or a device sensor", { navigate("health_connect") })
        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { .75f }, Modifier.size(130.dp), color = Naka.Violet, trackColor = Naka.Pale, strokeWidth = 5.dp)
            Surface(Modifier.size(100.dp).clickable { navigate(NakaRoutes.ProfileInfo) }, shape = CircleShape, color = Naka.Lavender) {
                Box(contentAlignment = Alignment.Center) { Text(uiState.userProfile?.name?.take(1)?.uppercase() ?: "N",
                    color = Naka.Violet, fontSize = 36.sp) }
            }
        }
        Text(uiState.userProfile?.name ?: "Fitness User", Modifier.align(Alignment.CenterHorizontally), fontSize = 25.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(uiState.userProfile?.email.orEmpty(), Modifier.align(Alignment.CenterHorizontally), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        NakaRow("User Profile Information", "Personal details and measurements", { navigate(NakaRoutes.ProfileInfo) })
        NakaRow("Progress", "Your activity and health records", { navigate(NakaRoutes.Progress) })
        NakaRow("Coach Recommendation", "Find a session for your goals", { navigate(NakaRoutes.Coaches) })
        NakaRow("Connection Device", "Manage your wearable", { navigate(NakaRoutes.Devices) })
        NakaRow("Manage Notifications", "Choose your reminders", { navigate(NakaRoutes.Notifications) })
        NakaPhotoCard("Premium Features", "Explore your next level", R.drawable.nakafit_gym_motivation, { navigate(NakaRoutes.Premium) }, height = 150)
        NakaRow("Set Your Goals", "Review your onboarding preferences", { navigate(Screen.Onboarding.route) })
        NakaRow("Workout Plan", "Your weekly training routine", { navigate(Screen.WorkoutPlan.route) })
        NakaRow("Food History", "Browse and manage your diary", { navigate(Screen.History.route) })
        NakaRow("Fitness Coach Chat", "Ask the local fitness assistant", { navigate(Screen.Chatbot.route) })
        NakaRow("Settings", "Preferences and appearance", { navigate(NakaRoutes.Settings) })
        NakaRow("Support & Feedback", "Share your feedback", { navigate(NakaRoutes.Support) })
        NakaButton("Log Out", onLogout, outlined = true)
    }
}

@Composable
fun ProfileInformationScreen(uiState: CalFitUiState, onBack: () -> Unit,
    onSave: (String, Double, Double) -> Unit) {
    var name by rememberSaveable { mutableStateOf(uiState.userProfile?.name ?: "") }
    var height by rememberSaveable { mutableStateOf((uiState.userProfile?.heightCm ?: 170.0).toString()) }
    var weight by rememberSaveable { mutableStateOf(uiState.latestWeightKg.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    NakaPage("User Profile Information", onBack) {
        NakaSection("Personal Information")
        listOf("Full name" to name, "Height (cm)" to height, "Weight (kg)" to weight).forEachIndexed { index, (label, value) ->
            OutlinedTextField(value, { when(index) { 0 -> name = it; 1 -> height = it; else -> weight = it } },
                Modifier.fillMaxWidth(), label = { Text(label) }, shape = RoundedCornerShape(28.dp), singleLine = true)
        }
        Text(uiState.userProfile?.email ?: "Account", color = MaterialTheme.colorScheme.onSurfaceVariant)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        NakaButton("Save Changes", {
            val h = height.toDoubleOrNull(); val w = weight.toDoubleOrNull()
            if (name.isBlank() || h == null || h !in 100.0..230.0 || w == null || w !in 30.0..300.0) error = "Enter your name and valid measurements"
            else { onSave(name.trim(), h, w); onBack() }
        })
    }
}

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit, darkMode: Boolean, onDarkMode: (Boolean) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nakafit_settings", 0) }
    val textColor = MaterialTheme.colorScheme.onBackground
    NakaPage("Manage Notifications", onBack) {
        Text("Stay on track", color = textColor, fontSize = 27.sp)
        Text("Choose which reminders you want to see.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Dark mode", color = textColor)
                Text(if (darkMode) "Use the dark kcalfit palette" else "Use the light kcalfit palette", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(checked = darkMode, onCheckedChange = onDarkMode,
                colors = SwitchDefaults.colors(checkedTrackColor = Naka.Violet))
        }
        listOf("Workout reminders", "Daily goals", "Meal reminders", "Progress updates").forEach { label ->
            var checked by remember { mutableStateOf(prefs.getBoolean(label, true)) }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f), color = textColor)
                Switch(checked, { checked = it; prefs.edit().putBoolean(label,it).apply() }, colors = SwitchDefaults.colors(checkedTrackColor = Naka.Violet))
            }
        }
        Text("Preferences are saved locally. Push notifications are not connected in this preview.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
fun DeviceConnectionScreen(onBack: () -> Unit) {
    var phase by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf("Apple Watch") }
    var paired by rememberSaveable { mutableStateOf(false) }
    NakaPage("Connection Device", onBack) {
        if (phase == 0) {
            Text("Connection Device", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Choose your device to preview the setup flow.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf("Apple Watch", "GARMIN", "fitbit", "POLAR", "SUUNTO").forEach { item ->
                Surface(Modifier.fillMaxWidth().height(56.dp).clickable { selected = item }, shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, if (selected == item) Naka.Violet else Naka.Line)) {
                    Box(contentAlignment = Alignment.Center) { Text(item, fontSize = 21.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            NakaButton("Connect Now", { phase = 1 })
        } else if (phase == 1) {
            Text("Connect Your $selected", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Review the device setup steps before continuing.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            NakaPhoto(R.drawable.nakafit_runner, "Fitness activity outdoors", Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(24.dp)))
            NakaButton("Set Up Now", { phase = 2 })
            NakaButton("Skip", onBack, outlined = true)
        } else {
            Text("Set Up $selected", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Default.Watch, null, Modifier.size(100.dp).align(Alignment.CenterHorizontally), tint = Naka.Violet)
            Text("1. Turn on your device and keep it nearby", color = MaterialTheme.colorScheme.onSurface)
            Text("2. Enable Bluetooth and follow your device’s mobile app", color = MaterialTheme.colorScheme.onSurface)
            NakaButton("Preview Complete Setup", { paired = true })
            Text("This is a UI demonstration. No device pairing or health synchronization is performed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
    if (paired) AlertDialog(onDismissRequest = { paired = false }, icon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(70.dp), tint = Naka.Violet) },
        title = { Text("Complete Setup") }, text = { Text("The preview setup is complete. Actual wearable synchronization requires a supported integration.") },
        confirmButton = { TextButton({ paired = false; onBack() }) { Text("Done") } })
}

@Composable
fun PremiumScreen(onBack: () -> Unit, onPlans: () -> Unit) {
    NakaPage("Premium Features", onBack) {
        NakaPhoto(R.drawable.nakafit_gym_motivation, "Training studio", Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp)))
        Text("Reach Your Next Level", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
        listOf("Personalized workout plans", "Detailed health insights", "Nutrition recommendations", "More ways to track your goals").forEach {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Naka.Violet); Spacer(Modifier.width(12.dp)); Text(it) }
        }
        NakaButton("View Subscription Plans", onPlans)
    }
}

@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf("Yearly") }
    var confirm by remember { mutableStateOf(false) }
    var subscribed by rememberSaveable { mutableStateOf(false) }
    NakaPage("Subscription Plan", onBack) {
        Text("Choose Your Plan", fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Explore premium plan options in this demo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf("Monthly" to "$9.99 / month", "Yearly" to "$59.99 / year").forEach { (plan, price) ->
            Surface(Modifier.fillMaxWidth().clickable { selected = plan }, shape = RoundedCornerShape(24.dp),
                color = if (selected == plan) Naka.Lavender else Naka.Pale, contentColor = if (selected == plan) Naka.Ink else MaterialTheme.colorScheme.onSurface, border = if (selected == plan) BorderStroke(1.dp,Naka.Violet) else null) {
                Column(Modifier.padding(22.dp)) { Text(plan, fontSize = 24.sp); Text(price, color = MaterialTheme.colorScheme.onSurfaceVariant); if (plan == "Yearly") Text("Best value", color = Naka.Violet, fontSize = 12.sp) }
            }
        }
        NakaSection("Plan Summary")
        Text("$selected plan selected", color = MaterialTheme.colorScheme.onSurface)
        NakaButton(if (subscribed) "Demo Plan Selected" else "Continue", { confirm = true })
        Text("Prices and subscription states are sample data. No payment will be processed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Confirm Demo Plan") },
        text = { Text("Select the $selected plan for this preview?") },
        confirmButton = { TextButton({ subscribed = true; confirm = false }) { Text("Select Plan") } },
        dismissButton = { TextButton({ confirm = false }) { Text("Cancel") } })
}

@Composable
fun SettingsScreen(onBack: () -> Unit, darkMode: Boolean, onDarkMode: (Boolean) -> Unit, onNotifications: () -> Unit) {
    var metric by rememberSaveable { mutableStateOf(true) }
    NakaPage("Settings", onBack) {
        NakaRow("Notifications", "Manage your reminder preferences", onNotifications)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Metric units", Modifier.weight(1f)); Switch(metric, { metric = it }) }
        Text(if (metric) "Centimeters and kilograms" else "Imperial preference selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Dark mode", Modifier.weight(1f)); Switch(darkMode, onDarkMode) }
        Text("Appearance is applied throughout kcalfit.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text("kcalfit · Version 1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SupportScreen(onBack: () -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    NakaPage("Support & Feedback", onBack) {
        Text("How can we help?", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(message, { message = it; submitted = false }, Modifier.fillMaxWidth().height(180.dp),
            placeholder = { Text("Share your feedback") }, shape = RoundedCornerShape(24.dp))
        NakaButton(if (submitted) "Feedback Saved" else "Save Feedback", { submitted = true }, enabled = message.isNotBlank())
        if (submitted) Text("Your feedback is saved for this session. A support service is not connected yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
