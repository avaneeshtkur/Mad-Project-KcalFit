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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onScanBarcodeLookup: (barcode: String) -> FoodItemEntity?,
    onAddFoodSubmit: (name: String, brand: String, mealType: String, calories: Int, protein: Double, carbs: Double, fat: Double, quantity: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToManualAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scannedProduct by remember { mutableStateOf<FoodItemEntity?>(null) }
    var isSimulatingScan by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Scanner", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Camera Scanning Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.Black, shape = RoundedCornerShape(20.dp))
                    .border(3.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isSimulatingScan) "Scanning Barcode..." else "Point Camera at Food Barcode",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Simulate Scan Action Button
            Button(
                onClick = {
                    isSimulatingScan = true
                    val match = onScanBarcodeLookup("890100100001") ?: FoodItemEntity(
                        name = "Oatmeal Bowl", brand = "Quaker", calories = 220, proteinGrams = 8.0, carbsGrams = 34.0, fatGrams = 4.5, barcode = "890100100001"
                    )
                    scannedProduct = match
                    isSimulatingScan = false
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Scan Product Barcode", fontWeight = FontWeight.Bold)
            }

            scannedProduct?.let { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Product Detected!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${product.name} (${product.brand})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("${product.calories} kcal • P: ${product.proteinGrams.toInt()}g  C: ${product.carbsGrams.toInt()}g  F: ${product.fatGrams.toInt()}g", style = MaterialTheme.typography.bodySmall)

                        Button(
                            onClick = {
                                onAddFoodSubmit(
                                    product.name, product.brand, MealType.BREAKFAST.displayName,
                                    product.calories, product.proteinGrams, product.carbsGrams, product.fatGrams, "1 serving"
                                )
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Log to Diary")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onNavigateToManualAdd,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Product Not Found? Manual Add")
            }
        }
    }
}
