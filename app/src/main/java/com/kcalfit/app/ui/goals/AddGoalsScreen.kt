package com.kcalfit.app.ui.goals

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.data.preferences.ActivityGoal
import com.kcalfit.app.data.preferences.ActivityGoalStore

private val Violet = Color(0xFF6500F5)
private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted = Color(0xFF818AA0)
private val Pale: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun AddGoalsScreen(userId: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { ActivityGoalStore(context) }
    var savedGoals by remember(userId) { mutableStateOf(store.get(userId)) }
    var title by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("Running") }
    var repeat by remember { mutableStateOf("Daily") }
    var selectedDays by remember { mutableStateOf(setOf(0, 2, 4)) }
    var everyDay by remember { mutableStateOf(false) }
    var reminder by remember { mutableStateOf("06:00") }
    var error by remember { mutableStateOf<String?>(null) }
    val activities = listOf("Running", "Weights", "Yoga", "Sports", "Swimming", "Cycling", "Walking", "Dance")
    val week = listOf("S", "M", "T", "W", "T", "F", "S")

    Box(modifier.fillMaxSize().background(Color(0xFFBFC0C2))) {
    Column(Modifier.fillMaxSize().padding(top=8.dp).clip(RoundedCornerShape(topStart=24.dp,topEnd=24.dp)).background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
        .imePadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(76.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text("Add Goals", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClose, modifier = Modifier.size(40.dp).background(Pale, CircleShape)) {
                Icon(Icons.Default.Close, "Close")
            }
        }
        Text("Goals Title", color = Ink, fontSize = 17.sp)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = title, onValueChange = { title = it; error = null },
            placeholder = { Text("Enter goals name", color = Muted, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Violet, unfocusedBorderColor = Color(0xFFDDE1E8)))
        Spacer(Modifier.height(18.dp))
        Text("Choose Activity", color = Ink, fontSize = 17.sp)
        Spacer(Modifier.height(10.dp))
        Column(Modifier.fillMaxWidth().background(Pale, RoundedCornerShape(24.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            activities.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { option ->
                        Surface(modifier = Modifier.weight(1f).height(68.dp).clickable { activity = option },
                            shape = CircleShape, color = if (activity == option) Color(0xFFE9DDFF) else MaterialTheme.colorScheme.surface,
                            border = if (activity == option) BorderStroke(1.dp, Violet) else null) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                    Icon(when(option) { "Running" -> Icons.Default.DirectionsRun; "Weights" -> Icons.Default.FitnessCenter; "Yoga" -> Icons.Default.SelfImprovement; "Sports" -> Icons.Default.SportsBasketball; "Swimming" -> Icons.Default.Pool; "Cycling" -> Icons.Default.DirectionsBike; "Walking" -> Icons.Default.DirectionsWalk; else -> Icons.Default.MusicNote },null,Modifier.size(24.dp),tint=if(activity==option)Violet else Ink)
                                    Text(option,fontSize=9.sp,color=if(activity==option)Violet else Ink,textAlign=TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Repeat", color = Ink, fontSize = 17.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { option ->
                Surface(modifier = Modifier.weight(1f).clickable { repeat = option },
                    shape = RoundedCornerShape(24.dp), color = if (repeat == option) MaterialTheme.colorScheme.primaryContainer else Pale,
                    border = if (repeat == option) BorderStroke(1.dp, Violet) else null) {
                    Text(option, Modifier.padding(vertical = 10.dp), color = if (repeat == option) Violet else Muted,
                        fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Select Days", color = Ink, fontSize = 17.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            week.forEachIndexed { index, day ->
                val selected = index in selectedDays
                Surface(modifier = Modifier.weight(1f).height(48.dp).clickable {
                    selectedDays = if (selected) selectedDays - index else selectedDays + index
                    everyDay = selectedDays.size == 7
                }, shape = CircleShape, color = if (selected) MaterialTheme.colorScheme.primaryContainer else Pale,
                    border = if (selected) BorderStroke(1.dp, Violet) else null) {
                    Box(contentAlignment = Alignment.Center) { Text(day, color = if (selected) Violet else Ink) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Every Days", Modifier.weight(1f), fontSize = 17.sp, color = Ink)
            Switch(checked = everyDay, onCheckedChange = { everyDay = it; selectedDays = if (it) (0..6).toSet() else emptySet() },
                colors = SwitchDefaults.colors(checkedTrackColor = Violet))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Remind me", Modifier.weight(1f), fontSize = 17.sp, color = Ink)
            TextButton(onClick = {
                val parts = reminder.split(":")
                TimePickerDialog(context, { _, hour, minute -> reminder = "%02d:%02d".format(hour, minute) },
                    parts[0].toInt(), parts[1].toInt(), true).show()
            }) { Text(reminder, color = Muted) }
        }
        if (error != null) Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            if (title.isBlank()) error = "Enter a goal title"
            else if (selectedDays.isEmpty() && repeat == "Daily") error = "Select at least one day"
            else {
                store.add(userId, ActivityGoal(title.trim(), activity, repeat, selectedDays, everyDay, reminder))
                savedGoals = store.get(userId)
                onClose()
            }
        }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Text("Add Goals") }
        if (savedGoals.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Your goals", fontSize = 17.sp, color = Ink)
            savedGoals.forEach { goal -> Text("${goal.title} · ${goal.activity} · ${goal.repeat}", Modifier.padding(top = 10.dp), color = Muted) }
        }
        Spacer(Modifier.height(24.dp))
    }
}
}
