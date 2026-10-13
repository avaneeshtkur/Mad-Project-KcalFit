package com.kcalfit.app.ui.water
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcalfit.app.ui.theme.WaterBlue
import com.kcalfit.app.ui.viewmodel.CalFitUiState
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WaterScreen(uiState:CalFitUiState,onAddWater:(Int)->Unit,onNavigateBack:()->Unit,modifier:Modifier=Modifier){val target=2500;val consumed=uiState.totalWaterMl;val progress=(consumed.toFloat()/target).coerceIn(0f,1f);var custom by remember{mutableStateOf("")};Scaffold(topBar={TopAppBar(title={Text("Water Intake",style=MaterialTheme.typography.titleLarge)},navigationIcon={IconButton(onNavigateBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}})},modifier=modifier){padding->Column(Modifier.fillMaxSize().padding(padding).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=WaterBlue.copy(alpha=.12f))){Column(Modifier.padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){Icon(Icons.Default.WaterDrop,null,tint=WaterBlue,modifier=Modifier.size(30.dp));Text("$consumed ml",style=MaterialTheme.typography.displayLarge,color=WaterBlue,fontWeight=FontWeight.ExtraBold);Text("of $target ml daily target",color=MaterialTheme.colorScheme.onSurfaceVariant);LinearProgressIndicator({progress},Modifier.fillMaxWidth().height(10.dp),color=WaterBlue);Text(if(progress>=1f)"Daily target complete" else if(consumed>0)"Good pace. Keep hydrating through the day." else "Start with a glass of water to begin your goal.",color=MaterialTheme.colorScheme.onSurfaceVariant)}};Text("Quick add",style=MaterialTheme.typography.titleMedium);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(250,500,750).forEach{amount->OutlinedButton({onAddWater(amount)},Modifier.weight(1f),shape=RoundedCornerShape(20.dp)){Text("+$amount ml")}}};Text("Custom amount",style=MaterialTheme.typography.titleMedium);Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(custom,{custom=it},Modifier.weight(1f),label={Text("Millilitres")},singleLine=true);Spacer(Modifier.width(8.dp));Button({custom.toIntOrNull()?.takeIf{it>0}?.let{onAddWater(it);custom=""}},shape=RoundedCornerShape(18.dp)){Text("Add")}};Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Hydration routine",style=MaterialTheme.typography.titleMedium);Text("250 ml after waking • 500 ml around training • 250 ml with each meal",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}
}
