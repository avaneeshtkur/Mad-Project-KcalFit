package com.kcalfit.app.ui.exercise
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.domain.activity.ActivityCatalog
import com.kcalfit.app.domain.activity.ActivityCategory
import com.kcalfit.app.ui.theme.ExerciseOrange
import com.kcalfit.app.ui.viewmodel.CalFitUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(uiState: CalFitUiState, onLogExerciseSubmit: (String, String, Int, Int) -> Unit,
    onDeleteExercise: (ExerciseEntry) -> Unit, onNavigateBack: () -> Unit, modifier: Modifier = Modifier,
    onStartLiveSession: (String) -> Unit = {}) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = selectedId?.let(ActivityCatalog::byId)
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val category = categoryName?.let(ActivityCategory::valueOf)
    var duration by remember { mutableStateOf("30") }
    var calories by remember { mutableStateOf("250") }
    val focus = LocalFocusManager.current
    Scaffold(topBar = { TopAppBar(title = { Text("Exercise & Workout Log", maxLines = 1, softWrap = false, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = { IconButton(onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }, modifier = modifier) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item(key = "log_card") {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Log a session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (selected == null) {
                            ActivitySearchControls(query, { query = it }, category, { categoryName = it?.name })
                        } else {
                            SelectedActivity(selected) { selectedId = null }
                            Text(selected.tracking.summary, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (selected.supportsLiveSession) {
                                OutlinedButton({ onStartLiveSession(selected.id) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                    Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Start live session")
                                }
                                Text("Or record a session you've already finished:", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text("Your phone usually can't come along for ${selected.name.lowercase()}, so record it here when you finish.",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(duration, { duration = it }, Modifier.weight(1f), label = { Text("Minutes") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                                OutlinedTextField(calories, { calories = it }, Modifier.weight(1f), label = { Text("Calories") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                            }
                            Button({ onLogExerciseSubmit(selected.name, selected.category.label, duration.toIntOrNull() ?: 30, calories.toIntOrNull() ?: 250) },
                                Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Log workout")
                            }
                        }
                    }
                }
            }
            if (selected == null) activityPickerItems(query, category,
                onSelect = { focus.clearFocus(); selectedId = it.id },
                onClear = { query = ""; categoryName = null })
            item(key = "today_output") {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ExerciseOrange.copy(alpha = .12f))) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = ExerciseOrange)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Today's training output", style = MaterialTheme.typography.labelLarge)
                            Text("${uiState.caloriesBurned} kcal burned", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item(key = "logged_header") { Text("Logged sessions", style = MaterialTheme.typography.titleMedium) }
            if (uiState.exerciseEntries.isEmpty()) item(key = "logged_empty") { Text("Your logged workouts will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(uiState.exerciseEntries, key = { "entry_${it.id}" }) { entry ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.exerciseName, style = MaterialTheme.typography.titleMedium)
                            Text("${entry.durationMinutes} min • ${entry.category}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${entry.caloriesBurned} kcal burned", color = ExerciseOrange, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton({ onDeleteExercise(entry) }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}
