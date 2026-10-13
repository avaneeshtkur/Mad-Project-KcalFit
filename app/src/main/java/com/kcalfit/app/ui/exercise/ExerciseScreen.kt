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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.ui.theme.ExerciseOrange
import com.kcalfit.app.ui.viewmodel.CalFitUiState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(uiState:CalFitUiState,onLogExerciseSubmit:(String,String,Int,Int)->Unit,onDeleteExercise:(ExerciseEntry)->Unit,onNavigateBack:()->Unit,modifier:Modifier=Modifier){var name by remember{mutableStateOf("Running / Jogging")};var duration by remember{mutableStateOf("30")};var calories by remember{mutableStateOf("250")};Scaffold(topBar={TopAppBar(title={Text("Exercise & Workout Log",maxLines=1,softWrap=false,style=MaterialTheme.typography.titleLarge)},navigationIcon={IconButton(onNavigateBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}})},modifier=modifier){padding->LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Log a session",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Exercise name")},singleLine=true);Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedTextField(duration,{duration=it},Modifier.weight(1f),label={Text("Minutes")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true);OutlinedTextField(calories,{calories=it},Modifier.weight(1f),label={Text("Calories")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true)};Button({onLogExerciseSubmit(name,"CARDIO",duration.toIntOrNull()?:30,calories.toIntOrNull()?:250)},Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Icon(Icons.Default.Add,null);Spacer(Modifier.width(6.dp));Text("Log workout")}}}};item{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=ExerciseOrange.copy(alpha=.12f))){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.LocalFireDepartment,null,tint=ExerciseOrange);Spacer(Modifier.width(12.dp));Column{Text("Today's training output",style=MaterialTheme.typography.labelLarge);Text("${uiState.caloriesBurned} kcal burned",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}}}};item{Text("Logged sessions",style=MaterialTheme.typography.titleMedium)};if(uiState.exerciseEntries.isEmpty())item{Text("Your logged workouts will appear here.",color=MaterialTheme.colorScheme.onSurfaceVariant)};items(uiState.exerciseEntries){entry->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(entry.exerciseName,style=MaterialTheme.typography.titleMedium);Text("${entry.durationMinutes} min • ${entry.category}",color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${entry.caloriesBurned} kcal burned",color=ExerciseOrange,fontWeight=FontWeight.SemiBold)};IconButton({onDeleteExercise(entry)}){Icon(Icons.Default.Delete,"Delete",tint=MaterialTheme.colorScheme.error)}}}}}}}
