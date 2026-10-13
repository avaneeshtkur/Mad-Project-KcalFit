package com.kcalfit.app.ui.onboarding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.UserEntity
@Composable fun CalorieSuggestionScreen(bmr:Int,tdee:Int,suggestedCalories:Int,proteinGrams:Int,carbsGrams:Int,fatGrams:Int,userProfile:UserEntity?,onAcceptGoal:(Int)->Unit,onGenerateWorkoutPlan:()->Unit,onSkipToDashboard:()->Unit){ var goal by remember { mutableStateOf(suggestedCalories.toString()) }; Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){ Text("Your Calorie Goal",style=MaterialTheme.typography.headlineSmall); Card(Modifier.fillMaxWidth()){ Column(Modifier.padding(18.dp)){Text("BMR: $bmr kcal");Text("TDEE: $tdee kcal");Text("Suggested Daily Goal",style=MaterialTheme.typography.labelLarge);Text("$suggestedCalories kcal",style=MaterialTheme.typography.headlineMedium);Text("Macros: Protein ${proteinGrams}g • Carbs ${carbsGrams}g • Fat ${fatGrams}g") } }; Text("Based on your profile, we recommend $suggestedCalories kcal/day."); OutlinedTextField(goal,{goal=it},label={Text("Daily calorie goal")},singleLine=true); Button({ goal.toIntOrNull()?.let(onAcceptGoal);onGenerateWorkoutPlan() },Modifier.fillMaxWidth()){Text("Generate Workout Plan")}; OutlinedButton({goal.toIntOrNull()?.let(onAcceptGoal);onSkipToDashboard()},Modifier.fillMaxWidth()){Text("Skip to Dashboard")} } }
