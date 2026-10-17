package com.kcalfit.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.domain.calculator.ActivityLevel
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.ui.nakafit.*

@Composable
fun OnboardingScreen(initialName: String = "Fitness User",
    onOnboardingCompleted: (String, Int, String, Double, Double, Double, String, String, Int) -> Unit,
    modifier: Modifier = Modifier) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    var age by rememberSaveable { mutableStateOf("25") }
    var sex by rememberSaveable { mutableStateOf("FEMALE") }
    var height by rememberSaveable { mutableFloatStateOf(170f) }
    var weight by rememberSaveable { mutableFloatStateOf(64f) }
    var target by rememberSaveable { mutableFloatStateOf(60f) }
    var goal by rememberSaveable { mutableStateOf(FitnessGoal.LOSE_WEIGHT.name) }
    var activity by rememberSaveable { mutableStateOf(ActivityLevel.MODERATELY_ACTIVE.name) }
    var days by rememberSaveable { mutableIntStateOf(3) }
    var error by remember { mutableStateOf<String?>(null) }
    val titles = listOf("Welcome to Calfit", "What is your goal?", "Let's get to know you", "What is your height?",
        "What is your weight?", "Your activity level", "You're all set!")
    NakaPage(if (step == 0) "Calfit" else "Set Your Goals", if (step > 0) ({ step-- }) else null) {
        LinearProgressIndicator(progress = { (step + 1) / 7f }, Modifier.fillMaxWidth().height(5.dp), color = Naka.Violet, trackColor = Naka.Lavender)
        Text("${step + 1} of 7", color = Naka.Muted, fontSize = 12.sp)
        AnimatedContent(step, label = "onboarding steps") { selectedStep ->
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(titles[selectedStep], fontSize = 29.sp, lineHeight = 35.sp, color = MaterialTheme.colorScheme.onSurface)
                when (selectedStep) {
                    0 -> {
                        NakaPhoto(R.drawable.nakafit_runner, "Runner on a track", Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(26.dp)))
                        Text("A few simple steps to make your health journey personal.", color = Naka.Muted)
                    }
                    1 -> FitnessGoal.entries.forEach { item ->
                        Surface(Modifier.fillMaxWidth().clickable { goal = item.name }, shape = RoundedCornerShape(24.dp),
                            color = if (goal == item.name) Naka.Lavender else Naka.Pale,
                            border = if (goal == item.name) BorderStroke(1.dp, Naka.Violet) else null) {
                            Text(item.displayName, Modifier.padding(22.dp), color = if (goal == item.name) Naka.Ink else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    2 -> {
                        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Your name") }, shape = RoundedCornerShape(28.dp))
                        OutlinedTextField(age, { age = it.filter(Char::isDigit).take(3) }, Modifier.fillMaxWidth(), label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(28.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("FEMALE", "MALE").forEach { item -> FilterChip(sex == item, { sex = item },
                                label = { Text(item.lowercase().replaceFirstChar(Char::uppercase)) }, modifier = Modifier.weight(1f)) }
                        }
                    }
                    3 -> {
                        Spacer(Modifier.height(60.dp))
                        Text("${height.toInt()}", Modifier.align(Alignment.CenterHorizontally), fontSize = 58.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("centimeters", Modifier.align(Alignment.CenterHorizontally), color = Naka.Violet)
                        Slider(height, { height = it }, valueRange = 100f..230f, steps = 129)
                        Text("Log and monitor your daily health data.", color = Naka.Muted)
                    }
                    4 -> {
                        Text("${"%.1f".format(weight)} kg", Modifier.align(Alignment.CenterHorizontally), fontSize = 44.sp, color = MaterialTheme.colorScheme.onSurface)
                        Slider(weight, { weight = it }, valueRange = 30f..200f)
                        Text("Target weight", color = Naka.Muted)
                        Text("${"%.1f".format(target)} kg", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                        Slider(target, { target = it }, valueRange = 30f..200f)
                    }
                    5 -> {
                        ActivityLevel.entries.forEach { item -> FilterChip(activity == item.name, { activity = item.name },
                            label = { Text(item.displayName) }, modifier = Modifier.fillMaxWidth()) }
                        Text("Exercise days per week", color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..7).forEach { day -> FilterChip(days == day, { days = day }, label = { Text("$day") }, modifier = Modifier.weight(1f)) }
                        }
                    }
                    6 -> {
                        NakaPhoto(R.drawable.nakafit_meditation, "Mindful recovery by the lake", Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)))
                        Text("Your plan starts with ${days} active days each week.", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${height.toInt()} cm · ${"%.1f".format(weight)} kg · Target ${"%.1f".format(target)} kg", color = Naka.Muted)
                    }
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        NakaButton(if (step == 6) "Let's Get Started" else "Continue", {
            if (step == 2 && (name.isBlank() || (age.toIntOrNull() ?: 0) !in 13..100)) error = "Enter your name and an age between 13 and 100"
            else { error = null; if (step < 6) step++ else onOnboardingCompleted(name.trim(), age.toInt(), sex,
                height.toDouble(), weight.toDouble(), target.toDouble(), activity, goal, days) }
        })
    }
}
