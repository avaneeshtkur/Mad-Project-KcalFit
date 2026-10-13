package com.kcalfit.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kcalfit.app.ui.components.CalorieGoalDialog
import com.kcalfit.app.ui.viewmodel.CalFitUiState

private fun readable(value: String) = value.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

@Composable
fun ProfileScreen(
    uiState: CalFitUiState,
    onNavigateToWeight: () -> Unit,
    onNavigateToWorkoutPlan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onUpdateGoal: (Int) -> Unit,
    onLogout: () -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val user = uiState.userProfile
    var showGoalDialog by remember { mutableStateOf(false) }
    if (showGoalDialog) CalorieGoalDialog(
        currentGoal = uiState.dailyGoal,
        onDismiss = { showGoalDialog = false },
        onConfirm = { onUpdateGoal(it); showGoalDialog = false }
    )
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text((user?.name ?: "U").split(' ').mapNotNull { it.firstOrNull() }.take(2)
                        .joinToString("").uppercase(), style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(user?.name ?: "Your profile", style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(user?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${user?.currentStreakDays ?: 0}-day streak", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        HorizontalDivider()
        Text("Body stats", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("${uiState.latestWeightKg} kg", "Current weight", Modifier.weight(1f))
            StatTile("${user?.heightCm ?: 0.0} cm", "Height", Modifier.weight(1f))
            StatTile("${user?.age ?: 0}", "Age", Modifier.weight(1f))
        }
        Text("Goals & activity", style = MaterialTheme.typography.titleLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                ProfileDetail("Daily calorie goal", "${uiState.dailyGoal} kcal", onClick = { showGoalDialog = true })
                HorizontalDivider()
                ProfileDetail("Fitness goal", readable(user?.fitnessGoal ?: "Lose weight"))
                HorizontalDivider()
                ProfileDetail("Target weight", "${user?.targetWeightKg ?: 0.0} kg")
                HorizontalDivider()
                ProfileDetail("Activity level", readable(user?.activityLevel ?: "Moderately active"))
                HorizontalDivider()
                ProfileDetail("Exercise days", "${user?.exerciseDaysPerWeek ?: 3} per week")
            }
        }
        Text("Your records", style = MaterialTheme.typography.titleLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)) {
            Column {
                ActionRow("Weight log", onNavigateToWeight)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ActionRow("Workout plan", onNavigateToWorkoutPlan)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ActionRow("Food history", onNavigateToHistory)
            }
        }
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Dark mode", style = MaterialTheme.typography.titleMedium)
                Text("Use a darker color scheme", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Log out")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileDetail(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge)
        if (onClick != null) { Spacer(Modifier.width(8.dp)); Icon(Icons.Default.Edit, "Edit $label", Modifier.size(16.dp)) }
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
