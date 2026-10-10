package com.kcalfit.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Auth Flow
    object Splash : Screen("splash", "Splash", Icons.Default.FitnessCenter)
    object Welcome : Screen("welcome", "Welcome", Icons.Default.FitnessCenter)
    object Login : Screen("login", "Login", Icons.Default.Person)
    object Register : Screen("register", "Register", Icons.Default.PersonAdd)
    object EmailVerification : Screen("email_verification", "Verify Email", Icons.Default.MarkEmailRead)
    object ForgotPassword : Screen("forgot_password", "Forgot Password", Icons.Default.Lock)
    
    // Onboarding
    object Onboarding : Screen("onboarding", "Setup", Icons.Default.Tune)
    
    // Main App Tabs
    object Home : Screen("home", "Home", Icons.Default.Home)
    object AddFood : Screen("add_food", "Add Food", Icons.Default.AddCircleOutline)
    object Exercise : Screen("exercise", "Exercise", Icons.Default.FitnessCenter)
    object Water : Screen("water", "Water", Icons.Default.WaterDrop)
    object Chatbot : Screen("chatbot", "Coach", Icons.Default.SmartToy)
    
    // Secondary Screens
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.PieChart)
    object History : Screen("history", "History", Icons.Default.History)
    object FoodSearch : Screen("food_search", "Search Food", Icons.Default.Search)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    object WorkoutPlan : Screen("workout_plan", "Workout Plan", Icons.Default.EventNote)
    object Weight : Screen("weight", "Weight", Icons.Default.MonitorWeight)
    object CalorieSuggestion : Screen("calorie_suggestion", "Your Goal", Icons.Default.TrackChanges)
    
    companion object {
        val bottomNavItems = listOf(Home, Exercise, Water, Chatbot, Profile)
    }
}
