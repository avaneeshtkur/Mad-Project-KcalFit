package com.kcalfit.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object AddFood : Screen("add_food", "Add Food", Icons.Default.AddCircleOutline)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.PieChart)
    object History : Screen("history", "History", Icons.Default.History)

    companion object {
        val bottomNavItems = listOf(Home, AddFood, Nutrition, History)
    }
}
