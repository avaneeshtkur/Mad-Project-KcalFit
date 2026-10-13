package com.kcalfit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.ui.nakafit.NakaPhoto
import com.kcalfit.app.data.model.FoodEntry

@Composable
fun MealDetailScreen(entry: FoodEntry?, onBack: () -> Unit) {
    val totalMacros = ((entry?.carbsGrams ?: 0.0) + (entry?.proteinGrams ?: 0.0) + (entry?.fatGrams ?: 0.0) + (entry?.fiberGrams ?: 0.0)).coerceAtLeast(1.0)
    Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(350.dp)) {
            NakaPhoto(R.drawable.nakafit_food, "Healthy chicken and vegetable meal", Modifier.fillMaxSize())
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(entry?.foodName ?: "Meal unavailable", Modifier.weight(1f), fontSize = 20.sp)
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF9DAE0)) {
                    Text("${entry?.calories ?: 0} kcal", Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFF8F8F9)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailMacro("Carbs", entry?.carbsGrams ?: 0.0, totalMacros, Color(0xFFD9BBFF))
                    DetailMacro("Protein", entry?.proteinGrams ?: 0.0, totalMacros, Color(0xFFFFD975))
                    DetailMacro("Fat", entry?.fatGrams ?: 0.0, totalMacros, Color(0xFFFFA8AC))
                    DetailMacro("Fiber", entry?.fiberGrams ?: 0.0, totalMacros, Color(0xFFA5D727))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Serving", fontSize = 18.sp)
            Text(entry?.quantity ?: "Not available", color = Color(0xFF818AA0))
            Spacer(Modifier.height(14.dp))
            Text("Ingredients", fontSize = 18.sp)
            Text(when(entry?.foodName) { "Broccoli Chicken Nut Medley" -> "Chicken Breast · Broccoli · Chickpeas · Nuts"; "Oatmeal with Boiled Egg" -> "Oats · Egg"; "Orange Juice" -> "Orange juice"; "Greek Yogurt" -> "Milk · Yogurt cultures"; else -> "Ingredients were not recorded for this meal." }, color = Color(0xFF818AA0))
        }
    }
}

@Composable
private fun DetailMacro(label: String, grams: Double, total: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(progress = { (grams / total).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.size(45.dp), color = color, trackColor = Color(0xFFE9ECEF), strokeWidth = 6.dp)
        Text(label, fontSize = 12.sp)
        Text("${(grams / total * 100).toInt()}% · ${grams.toInt()}g", fontSize = 10.sp, color = Color(0xFF818AA0))
    }
}
