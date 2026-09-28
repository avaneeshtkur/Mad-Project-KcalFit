package com.kcalfit.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.repository.AuthRepository
import com.kcalfit.app.ui.auth.AuthViewModel
import com.kcalfit.app.ui.auth.ForgotPasswordScreen
import com.kcalfit.app.ui.auth.LoginScreen
import com.kcalfit.app.ui.auth.RegisterScreen
import com.kcalfit.app.ui.auth.WelcomeScreen
import com.kcalfit.app.ui.onboarding.OnboardingScreen
import com.kcalfit.app.ui.onboarding.OnboardingViewModel
import com.kcalfit.app.ui.screens.AddFoodScreen
import com.kcalfit.app.ui.screens.HistoryScreen
import com.kcalfit.app.ui.screens.HomeScreen
import com.kcalfit.app.ui.screens.NutritionScreen
import com.kcalfit.app.ui.viewmodel.CalFitViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: CalFitViewModel,
    authViewModel: AuthViewModel,
    onboardingViewModel: OnboardingViewModel,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val onboardingUiState by onboardingViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine start destination based on session state
    val startDestination = when {
        userPreferences.isLoggedIn() && userPreferences.isOnboarded() -> Screen.Home.route
        userPreferences.isLoggedIn() && !userPreferences.isOnboarded() -> Screen.Onboarding.route
        else -> Screen.Welcome.route
    }

    // Show bottom bar only on main app screens
    val showBottomBar = currentRoute != null && currentRoute !in Screen.authRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        },
        modifier = modifier
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // --- Auth Screens ---
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Login.route) {
                // Navigate on successful login
                LaunchedEffect(authUiState.loggedInUser) {
                    authUiState.loggedInUser?.let { user ->
                        if (user.isOnboarded) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    }
                }

                LoginScreen(
                    onLoginSuccess = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    errorMessage = authUiState.errorMessage,
                    isLoading = authUiState.isLoading
                )
            }

            composable(Screen.Register.route) {
                // Navigate on successful registration
                LaunchedEffect(authUiState.registeredUser) {
                    authUiState.registeredUser?.let {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                }

                RegisterScreen(
                    onRegisterSuccess = { name, email, password ->
                        authViewModel.register(name, email, password)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    errorMessage = authUiState.errorMessage,
                    isLoading = authUiState.isLoading
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // --- Onboarding ---
            composable(Screen.Onboarding.route) {
                LaunchedEffect(onboardingUiState.isCompleted) {
                    if (onboardingUiState.isCompleted) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                OnboardingScreen(
                    onOnboardingCompleted = { name, age, sex, heightCm, weightKg, targetWeightKg, activityLevel, goal ->
                        onboardingViewModel.completeOnboarding(
                            name, age, sex, heightCm, weightKg, targetWeightKg, activityLevel, goal
                        )
                    }
                )
            }

            // --- Main App Screens ---
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onAddFoodClick = {
                        navController.navigate(Screen.AddFood.route)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    },
                    onUpdateGoal = { newGoal ->
                        viewModel.updateCalorieGoal(newGoal)
                    },
                    onMealCategoryClick = { meal ->
                        viewModel.setMealTypeFilter(meal.displayName)
                        navController.navigate(Screen.Nutrition.route)
                    }
                )
            }

            composable(Screen.AddFood.route) {
                AddFoodScreen(
                    onAddFoodSubmit = { foodName, mealType, calories, quantity, date ->
                        viewModel.addFoodEntry(
                            foodName = foodName,
                            mealType = mealType,
                            calories = calories,
                            quantity = quantity,
                            date = date
                        )
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Nutrition.route) {
                NutritionScreen(
                    uiState = uiState,
                    onDateSelected = { date ->
                        viewModel.setSelectedDate(date)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    uiState = uiState,
                    onSearchQueryChange = { query ->
                        viewModel.setSearchQuery(query)
                    },
                    onMealFilterChange = { mealFilter ->
                        viewModel.setMealTypeFilter(mealFilter)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    },
                    onSeedSampleData = {
                        viewModel.seedSampleData()
                    }
                )
            }
        }
    }
}
