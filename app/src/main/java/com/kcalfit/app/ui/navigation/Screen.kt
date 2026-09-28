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
    // Auth Screens (no icon needed, not in bottom nav)
    object Welcome : Screen("welcome", "Welcome", Icons.Default.Home)
    object Login : Screen("login", "Login", Icons.Default.Home)
    object Register : Screen("register", "Register", Icons.Default.Home)
    object ForgotPassword : Screen("forgot_password", "Forgot Password", Icons.Default.Home)

    // Onboarding Screen
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.Home)

    // Main App Screens
    object Home : Screen("home", "Home", Icons.Default.Home)
    object AddFood : Screen("add_food", "Add Food", Icons.Default.AddCircleOutline)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.PieChart)
    object History : Screen("history", "History", Icons.Default.History)

    companion object {
        // Only main app screens appear in bottom navigation
        val bottomNavItems = listOf(Home, AddFood, Nutrition, History)

        // Routes where bottom nav should be hidden
        val authRoutes = listOf(
            Welcome.route, Login.route, Register.route,
            ForgotPassword.route, Onboarding.route
        )
    }
}
