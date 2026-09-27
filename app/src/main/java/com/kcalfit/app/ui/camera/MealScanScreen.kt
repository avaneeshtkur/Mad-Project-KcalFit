package com.kcalfit.app.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.MealType

data class AiFoodEstimate(
    val foodName: String,
    val estimatedCalories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val confidencePercent: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScanScreen(
    onAddFoodSubmit: (name: String, brand: String, mealType: String, calories: Int, protein: Double, carbs: Double, fat: Double, quantity: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var detectedEstimate by remember { mutableStateOf<AiFoodEstimate?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Scan AI Recognition", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.DarkGray, shape = RoundedCornerShape(20.dp))
                    .border(3.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Point Camera at Meal Plate", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    isAnalyzing = true
                    detectedEstimate = AiFoodEstimate(
                        foodName = "Grilled Chicken Rice Bowl",
                        estimatedCalories = 540,
                        proteinGrams = 38.0,
                        carbsGrams = 52.0,
                        fatGrams = 9.0,
                        confidencePercent = 92
                    )
                    isAnalyzing = false
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isAnalyzing) "Analyzing Image..." else "Capture & Estimate Meal", fontWeight = FontWeight.Bold)
            }

            detectedEstimate?.let { estimate ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Detected: ${estimate.foodName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text("${estimate.confidencePercent}% match", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text("${estimate.estimatedCalories} kcal • P: ${estimate.proteinGrams.toInt()}g  C: ${estimate.carbsGrams.toInt()}g  F: ${estimate.fatGrams.toInt()}g", style = MaterialTheme.typography.bodySmall)

                        Text("Note: Food recognition is an estimate. You can edit serving before saving.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Button(
                            onClick = {
                                onAddFoodSubmit(
                                    estimate.foodName, "Meal Scan AI", MealType.LUNCH.displayName,
                                    estimate.estimatedCalories, estimate.proteinGrams, estimate.carbsGrams, estimate.fatGrams, "1 plate"
                                )
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm & Add to Diary")
                        }
                    }
                }
            }
        }
    }
}
