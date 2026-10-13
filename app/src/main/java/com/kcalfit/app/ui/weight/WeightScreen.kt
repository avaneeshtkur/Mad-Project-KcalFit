package com.kcalfit.app.ui.weight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.ui.viewmodel.CalFitUiState
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WeightScreen(uiState:CalFitUiState,onLogWeight:(Double)->Unit,onNavigateBack:()->Unit,modifier:Modifier=Modifier){var weight by remember{mutableStateOf(uiState.latestWeightKg.toString())};Scaffold(topBar={TopAppBar(title={Text("Weight Log")},navigationIcon={IconButton(onNavigateBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}})},modifier=modifier){padding->Column(Modifier.fillMaxSize().padding(padding).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Icon(Icons.Default.MonitorWeight,null,tint=MaterialTheme.colorScheme.primary);Text("Current weight",style=MaterialTheme.typography.titleMedium);Text("${uiState.latestWeightKg} kg",style=MaterialTheme.typography.displayLarge);Text("Log a new measurement to keep your progress current.",color=MaterialTheme.colorScheme.onSurfaceVariant)}};OutlinedTextField(weight,{weight=it},Modifier.fillMaxWidth(),label={Text("Weight (kg)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true);Button({weight.toDoubleOrNull()?.let(onLogWeight)},Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("Save weight")};Text("Tip: weigh under similar conditions each time for a clearer trend.",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
