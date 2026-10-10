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

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: CalFitViewModel,
    userPreferences: UserPreferences,
    authViewModel: Any? = null,
    onboardingViewModel: Any? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val generatedCode by viewModel.generatedVerificationCode.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                LoginScreen(
                    onLoginSuccess = { email, password ->
                        viewModel.loginUser(email, password) { success ->
                            if (success) {
                                val target = if (uiState.userProfile?.isOnboarded == true) Screen.Home.route else Screen.Onboarding.route
                                navController.navigate(target) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            } else {
                                loginError = "Invalid email or password"
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
                    errorMessage = loginError
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { name, email, password ->
                        viewModel.registerUser(name, email, password)
                        navController.navigate(Screen.EmailVerification.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.EmailVerification.route) {
                EmailVerificationScreen(
                    email = uiState.userProfile?.email ?: "your email",
                    generatedCode = generatedCode,
                    onVerifyCode = { code ->
                        viewModel.verifyEmailCode(code)
                    },
                    onSuccess = {
                        viewModel.markEmailVerified()
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onResendCode = {
                        viewModel.resendVerificationCode()
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
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
                OnboardingScreen(
                    initialName = uiState.userProfile?.name ?: "Fitness User",
                    onOnboardingCompleted = { name, age, sex, heightCm, weightKg, targetWeightKg, activityLevel, goal ->
                        viewModel.completeOnboarding(
                            name, age, sex, heightCm, weightKg, targetWeightKg, activityLevel, goal
                        )
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // --- Main App Tabs ---
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Coach / Chatbot Screen", style = MaterialTheme.typography.titleLarge)
                }
            }

            composable(Screen.Profile.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Profile Screen", style = MaterialTheme.typography.titleLarge)
                }
            }

            // --- Secondary Screens ---
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

            composable(Screen.Weight.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Weight Log Screen", style = MaterialTheme.typography.titleLarge)
                }
            }

            composable(Screen.CalorieSuggestion.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Calorie Goal Suggestion Screen", style = MaterialTheme.typography.titleLarge)
                }
            }

            composable(Screen.WorkoutPlan.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Workout Plan Screen", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
