package com.kcalfit.app.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.domain.calculator.*
import com.kcalfit.app.domain.workout.ExerciseLibrary
import com.kcalfit.app.ui.exercise.ExerciseDemoSheet
import com.kcalfit.app.ui.exercise.ExerciseDetailSheet
import com.kcalfit.app.ui.nakafit.*

@Composable
fun WorkoutPlanScreen(plan: List<WorkoutDayPlan>, goal: String, daysPerWeek: Int,
    onAccept: () -> Unit, onRegenerate: () -> Unit, onBack: () -> Unit = onAccept) {
    val context=LocalContext.current
    val prefs=remember { context.getSharedPreferences("nakafit_training_preferences",0) }
    var days by rememberSaveable { mutableIntStateOf(prefs.getInt("days",daysPerWeek)) }
    var selectedGoal by rememberSaveable { mutableStateOf(prefs.getString("goal",goal) ?: goal) }
    val goalEnum=FitnessGoal.entries.firstOrNull { it.name==selectedGoal } ?: FitnessGoal.LOSE_WEIGHT
    val displayed=remember(days,selectedGoal,plan) {
        if (plan.isNotEmpty() && days==daysPerWeek && selectedGoal==goal) plan
        else WorkoutPlanGenerator.generatePlan(goalEnum,days,ActivityLevel.MODERATELY_ACTIVE)
    }
    var opened by remember { mutableStateOf<PlannedExercise?>(null) }
    NakaPage("Training Plan",onBack) {
        NakaPhoto(R.drawable.nakafit_gym_motivation,"Strength training studio",Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(24.dp)))
        Text("Your $days-Day Plan",fontSize=28.sp,color=Naka.Ink)
        Text("Choose your weekly routine and review each exercise.",color=Naka.Muted)
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            (1..7).forEach { day -> FilterChip(days==day,{ days=day },label={ Text("$day") },modifier=Modifier.weight(1f)) }
        }
        FitnessGoal.entries.forEach { item -> FilterChip(selectedGoal==item.name,{ selectedGoal=item.name },label={ Text(item.displayName) },modifier=Modifier.fillMaxWidth()) }
        displayed.forEach { day ->
            var expanded by remember(day) { mutableStateOf(false) }
            Surface(shape=RoundedCornerShape(22.dp),color=Naka.Pale) {
                Column(Modifier.padding(16.dp)) {
                    NakaRow(day.dayLabel,day.focusArea,{ expanded=!expanded })
                    if(expanded) day.exercises.forEach { exercise ->
                        HorizontalDivider()
                        // Tapping opens the exercise with its animation.
                        Column(Modifier.fillMaxWidth().clickable { opened=exercise }) {
                            Text(exercise.name,Modifier.padding(top=12.dp),color=Naka.Ink,fontSize=17.sp)
                            Text("${exercise.sets} sets × ${exercise.reps} · ${exercise.restSeconds}s rest",color=Naka.Violet,fontSize=13.sp)
                            Text(exercise.instruction,color=Naka.Muted,fontSize=12.sp)
                            Text("Tap to see how it's done",Modifier.padding(bottom=12.dp),color=Naka.Violet,fontSize=12.sp)
                        }
                    }
                }
            }
        }
        NakaButton("Save Training Plan",{ prefs.edit().putInt("days",days).putString("goal",selectedGoal).apply();onAccept() })
        NakaButton("Refresh Plan",onRegenerate,outlined=true)
    }
    opened?.let { exercise ->
        val known=ExerciseLibrary.find(exercise.name)
        if(known!=null) ExerciseDetailSheet(known,null) { opened=null }
        else ExerciseDemoSheet(exercise.name,listOf("${exercise.sets} sets × ${exercise.reps} · ${exercise.restSeconds}s rest",exercise.instruction)) { opened=null }
    }
}
