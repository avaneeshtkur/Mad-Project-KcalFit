package com.kcalfit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.ui.components.getMealTypeDetails
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickFoodPreset(
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val quantity: String
)

val sampleFoodPresets = listOf(
    QuickFoodPreset("Oatmeal", MealType.BREAKFAST, 250, "1 bowl (200g)"),
    QuickFoodPreset("Boiled Eggs", MealType.BREAKFAST, 155, "2 large eggs"),
    QuickFoodPreset("Banana", MealType.SNACK, 105, "1 medium"),
    QuickFoodPreset("Apple", MealType.SNACK, 95, "1 medium"),
    QuickFoodPreset("Rice Bowl", MealType.LUNCH, 320, "1 cup"),
    QuickFoodPreset("Chicken Breast", MealType.LUNCH, 280, "150g"),
    QuickFoodPreset("Dal Tadka", MealType.DINNER, 220, "1 bowl"),
    QuickFoodPreset("Roti / Chapati", MealType.DINNER, 120, "1 piece"),
    QuickFoodPreset("Milk", MealType.BREAKFAST, 150, "1 glass (250ml)"),
    QuickFoodPreset("Veg Sandwich", MealType.LUNCH, 290, "2 slices")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFoodScreen(
    onAddFoodSubmit: (foodName: String, mealType: String, calories: Int, quantity: String, date: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDateString = remember { dateFormat.format(Date()) }

    var foodName by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var caloriesText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(todayDateString) }

    var foodNameError by remember { mutableStateOf<String?>(null) }
    var caloriesError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Food Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Quick Preset Suggestions Header & Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick Food Suggestions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Tap a food item to quickly fill details:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleFoodPresets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable {
                                    foodName = preset.name
                                    selectedMealType = preset.mealType
                                    caloriesText = preset.calories.toString()
                                    quantityText = preset.quantity
                                    foodNameError = null
                                    caloriesError = null
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${preset.calories} kcal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Meal Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Food Name Input
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = {
                            foodName = it
                            if (it.isNotBlank()) foodNameError = null
                        },
                        label = { Text("Food Name *") },
                        placeholder = { Text("e.g. Oatmeal, Rice, Chicken Curry") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Fastfood, contentDescription = null)
                        },
                        isError = foodNameError != null,
                        supportingText = {
                            foodNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. Meal Type Selector
                    Column {
                        Text(
                            text = "Meal Type *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MealType.entries.forEach { meal ->
                                val selected = selectedMealType == meal
                                val (mealColor, _) = getMealTypeDetails(meal)

                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedMealType = meal },
                                    label = {
                                        Text(
                                            text = meal.displayName,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = mealColor.copy(alpha = 0.2f),
                                        selectedLabelColor = mealColor,
                                        selectedLeadingIconColor = mealColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 3. Calories Input
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = {
                            caloriesText = it
                            if (it.toIntOrNull() != null && it.toInt() > 0) caloriesError = null
                        },
                        label = { Text("Calories (kcal) *") },
                        placeholder = { Text("e.g. 250") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = caloriesError != null,
                        supportingText = {
                            caloriesError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 4. Quantity / Serving Input (Optional)
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity / Serving (Optional)") },
                        placeholder = { Text("e.g. 1 bowl, 200g, 2 pieces") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Scale, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 5. Date Field
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Food Submit Button
            Button(
                onClick = {
                    var isValid = true

                    if (foodName.isBlank()) {
                        foodNameError = "Please enter a food name"
                        isValid = false
                    } else {
                        foodNameError = null
                    }

                    val caloriesVal = caloriesText.toIntOrNull()
                    if (caloriesVal == null || caloriesVal <= 0) {
                        caloriesError = "Please enter a valid calorie amount (> 0)"
                        isValid = false
                    } else {
                        caloriesError = null
                    }

                    if (isValid && caloriesVal != null) {
                        onAddFoodSubmit(
                            foodName.trim(),
                            selectedMealType.displayName,
                            caloriesVal,
                            if (quantityText.isBlank()) "1 serving" else quantityText.trim(),
                            dateString
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged '${foodName.trim()}' (${caloriesVal} kcal)!")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Add Food Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
