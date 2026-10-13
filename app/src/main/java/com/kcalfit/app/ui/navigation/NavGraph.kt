package com.kcalfit.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.ui.auth.EmailVerificationScreen
import com.kcalfit.app.ui.auth.ForgotPasswordScreen
import com.kcalfit.app.ui.auth.LoginScreen
import com.kcalfit.app.ui.auth.RegisterScreen
import com.kcalfit.app.ui.auth.WelcomeScreen
import com.kcalfit.app.ui.exercise.ExerciseScreen
import com.kcalfit.app.ui.onboarding.OnboardingScreen
import com.kcalfit.app.ui.screens.AddFoodScreen
import com.kcalfit.app.ui.screens.HistoryScreen
import com.kcalfit.app.ui.screens.HomeScreen
import com.kcalfit.app.ui.screens.NutritionScreen
import com.kcalfit.app.ui.splash.SplashScreen
import com.kcalfit.app.ui.viewmodel.CalFitViewModel
import com.kcalfit.app.ui.water.WaterScreen
import com.kcalfit.app.ui.onboarding.CalorieSuggestionScreen
import com.kcalfit.app.ui.workout.WorkoutPlanScreen
import com.kcalfit.app.ui.chatbot.ChatbotScreen
import com.kcalfit.app.ui.profile.ProfileScreen
import com.kcalfit.app.ui.weight.WeightScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: CalFitViewModel,
    userPreferences: UserPreferences,
    darkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val authReady by viewModel.authReady.collectAsState()
    val pendingEmail by viewModel.pendingEmail.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var addFoodMeal by remember { mutableStateOf(com.kcalfit.app.data.model.MealType.BREAKFAST) }

    val bottomNavRoutes = Screen.bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in bottomNavRoutes

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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // --- Splash ---
            composable(Screen.Splash.route) {
                SplashScreen(
                    userProfile = uiState.userProfile,
                    authReady = authReady,
                    onNavigateNext = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

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
                var loginError by remember { mutableStateOf<String?>(null) }
                var loginLoading by remember { mutableStateOf(false) }
                LoginScreen(
                    onLoginSuccess = { email, password ->
                        loginLoading = true
                        loginError = null
                        viewModel.loginUser(email, password) { error, onboarded ->
                            loginLoading = false
                            if (error == null) {
                                val target = if (onboarded) Screen.Home.route else Screen.Onboarding.route
                                navController.navigate(target) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            } else {
                                loginError = error
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    errorMessage = loginError,
                    isLoading = loginLoading
                )
            }

            composable(Screen.Register.route) {
                var registerError by remember { mutableStateOf<String?>(null) }
                var registerLoading by remember { mutableStateOf(false) }
                RegisterScreen(
                    onRegisterSuccess = { name, email, password ->
                        registerLoading = true
                        registerError = null
                        viewModel.registerUser(name, email, password) { error ->
                            registerLoading = false
                            if (error == null) navController.navigate(Screen.EmailVerification.route)
                            else registerError = error
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    errorMessage = registerError,
                    isLoading = registerLoading
                )
            }

            composable(Screen.EmailVerification.route) {
                EmailVerificationScreen(
                    email = pendingEmail.ifBlank { userPreferences.getUserEmail() },
                    onVerifyCode = { code, result -> viewModel.verifyEmailCode(code, result) },
                    onSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onResendCode = { result -> viewModel.resendVerificationCode(result) },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onRequestReset = { email, result -> viewModel.requestPasswordReset(email, result) },
                    onNavigateBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // --- Onboarding ---
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    initialName = uiState.userProfile?.name ?: "Fitness User",
                    onOnboardingCompleted = { name, age, sex, heightCm, weightKg, targetWeightKg, activityLevel, goal, exerciseDays ->
                        viewModel.completeOnboarding(
                            name, age, sex, heightCm, weightKg, targetWeightKg, activityLevel, goal, exerciseDays
                        )
                        navController.navigate(Screen.CalorieSuggestion.route)
                    }
                )
            }

            // --- Main App Tabs ---
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onAddFoodClick = { meal ->
                        addFoodMeal = meal ?: com.kcalfit.app.data.model.MealType.BREAKFAST
                        navController.navigate(Screen.AddFood.route)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    },
                    onUpdateGoal = { newGoal ->
                        viewModel.updateCalorieGoal(newGoal)
                    },
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onOpenExercise = { navController.navigate(Screen.Exercise.route) }
                )
            }

            composable(Screen.Exercise.route) {
                ExerciseScreen(
                    uiState = uiState,
                    onLogExerciseSubmit = { name, category, duration, calories ->
                        viewModel.logExercise(name, category, duration, calories)
                    },
                    onDeleteExercise = { entry ->
                        viewModel.deleteExerciseEntry(entry)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Water.route) {
                WaterScreen(
                    uiState = uiState,
                    onAddWater = { amount ->
                        viewModel.addWaterIntake(amount)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Chatbot.route) {
                ChatbotScreen(uiState = uiState, onSendMessage = viewModel::sendCoachQuery)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    uiState = uiState,
                    onNavigateToWeight = { navController.navigate(Screen.Weight.route) },
                    onNavigateToWorkoutPlan = { viewModel.generateWorkoutPlan(); navController.navigate(Screen.WorkoutPlan.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onUpdateGoal = viewModel::updateCalorieGoal,
                    onLogout = { viewModel.logout(); navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } } },
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }

            // --- Secondary Screens ---
            composable(Screen.AddFood.route) {
                AddFoodScreen(
                    initialMealType = addFoodMeal,
                    searchResults = uiState.masterFoodSearchResults,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onAddFoodSubmit = { foodName, mealType, calories, protein, carbs, fat, fiber, sugar, sodium, quantity, date ->
                        viewModel.addFoodEntry(
                            foodName = foodName,
                            mealType = mealType,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat,
                            fiber = fiber,
                            sugar = sugar,
                            sodium = sodium,
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

            composable(Screen.Weight.route) {
                WeightScreen(uiState = uiState, onLogWeight = { viewModel.logWeight(it) }, onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.CalorieSuggestion.route) {
                val result by viewModel.onboardingResult.collectAsState()
                result?.let { calc -> CalorieSuggestionScreen(calc.bmr, calc.tdee, calc.dailyCalorieTarget, uiState.targetProteinGrams, uiState.targetCarbsGrams, uiState.targetFatGrams, uiState.userProfile, viewModel::updateCalorieGoal, { viewModel.generateWorkoutPlan(); navController.navigate(Screen.WorkoutPlan.route) }, { navController.navigate(Screen.Home.route) }) }
            }

            composable(Screen.WorkoutPlan.route) {
                val plan by viewModel.workoutPlan.collectAsState(); WorkoutPlanScreen(plan, uiState.userProfile?.fitnessGoal ?: "LOSE_WEIGHT", uiState.userProfile?.exerciseDaysPerWeek ?: 3, { navController.navigate(Screen.Home.route) }, viewModel::generateWorkoutPlan)
            }
        }
    }
}
