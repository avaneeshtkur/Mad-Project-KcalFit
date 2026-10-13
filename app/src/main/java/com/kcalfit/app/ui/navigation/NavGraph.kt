package com.kcalfit.app.ui.navigation

import android.net.Uri
import com.kcalfit.app.domain.chatbot.CoachDestination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kcalfit.app.data.model.*
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.ui.auth.*
import com.kcalfit.app.ui.exercise.ExerciseScreen
import com.kcalfit.app.ui.goals.AddGoalsScreen
import com.kcalfit.app.ui.nakafit.*
import com.kcalfit.app.ui.onboarding.OnboardingScreen
import com.kcalfit.app.ui.onboarding.CalorieSuggestionScreen
import com.kcalfit.app.ui.screens.*
import com.kcalfit.app.ui.splash.SplashScreen
import com.kcalfit.app.ui.viewmodel.CalFitViewModel
import com.kcalfit.app.ui.water.WaterScreen
import com.kcalfit.app.ui.weight.WeightScreen
import com.kcalfit.app.ui.workout.WorkoutPlanScreen
import com.kcalfit.app.ui.chatbot.ChatbotScreen

@Composable
fun NavGraph(navController: NavHostController, viewModel: CalFitViewModel, userPreferences: UserPreferences,
    darkMode: Boolean = false, onDarkModeChange: (Boolean) -> Unit = {}, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val authReady by viewModel.authReady.collectAsState()
    val authenticated by viewModel.authenticated.collectAsState()
    val pendingEmail by viewModel.pendingEmail.collectAsState()
    val entry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var addFoodMeal by rememberSaveable { mutableStateOf(MealType.BREAKFAST) }
    var selectedCatalogueFood by remember { mutableStateOf<FoodItemEntity?>(null) }
    var foodMutationError by remember { mutableStateOf<String?>(null) }
    var chatX by rememberSaveable { mutableFloatStateOf(0f) }
    var chatY by rememberSaveable { mutableFloatStateOf(.92f) }

    fun navigate(route: String) { navController.navigate(route) { launchSingleTop = true } }
    fun back() { navController.popBackStack() }
    fun openMeal(food: FoodEntry) { navigate("meal_detail/${food.id}") }
    fun openWorkout(id: String) { navigate("workout_detail/$id") }
    fun addFood(meal: MealType) { selectedCatalogueFood=null;addFoodMeal = meal; navigate(Screen.AddFood.route) }
    fun selectFood(food: FoodItemEntity) { selectedCatalogueFood=food;navigate(Screen.AddFood.route) }
    fun openDiary(meal: MealType) { navigate("meal_diary/${meal.name}") }
    fun updateMeasurements(height: Double?, weight: Double?) { viewModel.updateLocalProfile(height = height, weight = weight) }
    fun addWater(amount: Int) { viewModel.addWaterIntake(amount) }
    fun logWeight(weight: Double) { viewModel.logWeight(weight) }
    fun logExercise(name: String, category: String, duration: Int, calories: Int) { viewModel.logExercise(name, category, duration, calories) }
    fun updateGoal(goal: Int) { viewModel.updateCalorieGoal(goal) }
    fun deleteFood(food: FoodEntry) { viewModel.removeFoodEntry(food) { foodMutationError = it } }

    val currentRoute = entry?.destination?.route
    LaunchedEffect(authReady, authenticated, currentRoute) {
        val publicRoutes = setOf(Screen.Splash.route, Screen.Welcome.route, Screen.Login.route,
            Screen.Register.route, Screen.EmailVerification.route, Screen.ForgotPassword.route)
        if (authReady && !authenticated && currentRoute != null && currentRoute !in publicRoutes) {
            navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        }
    }
    val bottomRoutes = Screen.bottomNavItems.map { it.route }.toSet()
    val showChatbot = currentRoute in setOf(
        Screen.Home.route, Screen.Track.route, Screen.Nutrition.route, Screen.Insight.route,
        Screen.Profile.route, Screen.Exercise.route, NakaRoutes.Progress, NakaRoutes.HealthSummary,
        NakaRoutes.Coaches
    )
    Scaffold(bottomBar = { if (currentRoute in bottomRoutes) BottomNavigationBar(navController) },
        modifier = modifier) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        NavHost(navController, startDestination = Screen.Splash.route, modifier = Modifier.fillMaxSize()) {
            composable(Screen.Splash.route) {
                SplashScreen(uiState.userProfile, authReady, { target -> navController.navigate(target) { popUpTo(Screen.Splash.route) { inclusive = true } } })
            }
            composable(Screen.Welcome.route) {
                WelcomeScreen(onNavigateToLogin = { navigate(Screen.Login.route) }, onNavigateToRegister = { navigate(Screen.Register.route) })
            }
            composable(Screen.Login.route) {
                var error by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(false) }
                LoginScreen(onLoginSuccess = { email, password, savePassword ->
                    loading = true; error = null
                    viewModel.loginUser(email, password) { result, onboarded ->
                        if (result == null) scope.launch {
                            if (savePassword) {
                                try { PasswordManager(context).save(context, email, password) }
                                catch (_: androidx.credentials.exceptions.CreateCredentialCancellationException) { }
                                catch (_: Exception) {
                                    android.widget.Toast.makeText(context, "Signed in. Password saving is unavailable from this provider.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                            loading = false
                            navController.navigate(if (onboarded) Screen.Home.route else Screen.Onboarding.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                        } else { loading = false; error = result }
                    }
                }, onNavigateToRegister = { navigate(Screen.Register.route) }, onNavigateToForgotPassword = { navigate(Screen.ForgotPassword.route) }, errorMessage = error, isLoading = loading)
            }
            composable(Screen.Register.route) {
                var error by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(false) }
                RegisterScreen(onRegisterSuccess = { name, email, password ->
                    loading = true; error = null
                    viewModel.registerUser(name,email,password) { result -> loading = false; if (result == null) { navigate(Screen.EmailVerification.route) } else error = result }
                }, onNavigateToLogin = { navigate(Screen.Login.route) }, errorMessage = error, isLoading = loading)
            }
            composable(Screen.EmailVerification.route) {
                EmailVerificationScreen(pendingEmail.ifBlank { userPreferences.getUserEmail() }, viewModel::verifyEmailCode,
                    { navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } },
                    viewModel::resendVerificationCode, { navigate(Screen.Login.route) })
            }
            composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(viewModel::requestPasswordReset, ::back) }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(uiState.userProfile?.name ?: "", { name, age, sex, height, weight, target, activity, goal, days ->
                    viewModel.completeOnboarding(name,age,sex,height,weight,target,activity,goal,days)
                    navigate(Screen.CalorieSuggestion.route)
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(uiState, { addFood(it ?: MealType.BREAKFAST) }, ::deleteFood,
                    ::updateGoal, darkMode, onDarkModeChange, { navigate(Screen.Exercise.route) }, { openDiary(MealType.BREAKFAST) },
                    { navigate(Screen.AddGoals.route) }, { navigate(Screen.Profile.route) }, { navigate(Screen.Track.route) },
                    { navigate(NakaRoutes.Notifications) }, ::openWorkout, onMeal = ::openDiary, onFood = ::openMeal)
            }
            composable(Screen.Exercise.route) {
                WorkoutDiscoveryScreen(::openWorkout, { navigate(NakaRoutes.ExerciseLog) },
                    { viewModel.generateWorkoutPlan(); navigate(Screen.WorkoutPlan.route) }, ::back)
            }
            composable(NakaRoutes.WorkoutDetail) { destination -> WorkoutDetailScreen(destination.arguments?.getString("id") ?: "strength", ::back,
                { navigate("workout_session/${destination.arguments?.getString("id") ?: "strength"}") }) }
            composable(NakaRoutes.WorkoutSession) { destination ->
                val workout = workoutForId(destination.arguments?.getString("id") ?: "strength")
                WorkoutSessionScreen(workout.id, ::back) { minutes, calories ->
                    logExercise(workout.title,workout.category,minutes,calories)
                    navController.navigate(Screen.Track.route) { popUpTo(Screen.Home.route); launchSingleTop = true }
                }
            }
            composable(NakaRoutes.ExerciseLog) { ExerciseScreen(uiState, ::logExercise,
                viewModel::deleteExerciseEntry, ::back) }
            composable(Screen.Track.route) {
                TrackScreen(uiState, userPreferences.getAuthUserId(),
                    { title,activity -> navigate("daily_goal/${Uri.encode(title)}/${Uri.encode(activity)}") }, { navigate(Screen.AddGoals.route) }, { navigate(NakaRoutes.Progress) },
                    { navigate(NakaRoutes.HealthSummary) }, { navigate(NakaRoutes.Sleep) }, { navigate(NakaRoutes.Period) }, { navigate(Screen.Water.route) },
                    ::openDiary, ::addFood, ::openMeal, ::deleteFood)
            }
            composable(Screen.AddGoals.route) { AddGoalsScreen(userPreferences.getAuthUserId(), ::back) }
            composable(NakaRoutes.DailyGoal) { destination ->
                val title=destination.arguments?.getString("title") ?: "Morning Run"
                val activity=destination.arguments?.getString("activity") ?: "Running"
                DailyGoalScreen(title,activity,userPreferences.getAuthUserId(),::back, {
                    when (activity) { "Yoga" -> openWorkout("yoga"); "Weights" -> openWorkout("weights"); "Running" -> openWorkout("run"); else -> navigate(NakaRoutes.ExerciseLog) }
                }, uiState, ::openDiary, ::addFood, ::openMeal, ::deleteFood)
            }
            composable(NakaRoutes.Progress) { ProgressScreen(uiState,::back,::updateMeasurements,{ navigate(NakaRoutes.HealthLog) }) }
            composable(NakaRoutes.HealthSummary) { HealthSummaryScreen(uiState,::back,{ navigate(NakaRoutes.HealthLog) }) }
            composable(NakaRoutes.HealthLog) { HealthLogScreen(::back,::logWeight,::addWater) }
            composable(NakaRoutes.Sleep) { SleepScreen(::back) }
            composable(NakaRoutes.Period) { PeriodScreen(::back) }
            composable(Screen.Insight.route) { InsightScreen(uiState,{ navigate(Screen.Track.route) },{ navigate(NakaRoutes.Progress) }) }
            composable(Screen.Water.route) { WaterScreen(uiState,::addWater,::back) }
            composable(Screen.Weight.route) { WeightScreen(uiState,::logWeight,::back) }
            composable(Screen.Nutrition.route) {
                NutritionMenuScreen(uiState, ::openDiary, { navigate(NakaRoutes.Search) })
            }
            composable("meal_diary/{meal}") { destination ->
                val meal = MealType.fromString(destination.arguments?.getString("meal") ?: "Breakfast")
                NutritionScreen(uiState, viewModel::setSelectedDate, ::deleteFood, { addFood(meal) },
                    { addFoodMeal = meal; navigate(Screen.BarcodeLookup.route) }, onBack = ::back, onMealClick = ::openMeal, meal = meal)
            }
            composable(NakaRoutes.Breakfast) {
                NutritionScreen(uiState, viewModel::setSelectedDate, ::deleteFood, { addFood(MealType.BREAKFAST) },
                    { addFoodMeal = MealType.BREAKFAST; navigate(Screen.BarcodeLookup.route) }, onBack = ::back, onMealClick = ::openMeal)
            }
            composable(NakaRoutes.Suggestions) { SearchScreen(uiState, ::back, viewModel::setSearchQuery, ::openMeal, ::openWorkout, { addFood(addFoodMeal) }, ::selectFood) }
            composable(NakaRoutes.Search) { SearchScreen(uiState, ::back, viewModel::setSearchQuery, ::openMeal, ::openWorkout, { addFood(addFoodMeal) }, ::selectFood) }
            composable(Screen.FoodSearch.route) { SearchScreen(uiState, ::back, viewModel::setSearchQuery, ::openMeal, ::openWorkout, { addFood(addFoodMeal) }, ::selectFood) }
            composable(Screen.MealDetail.route) { destination ->
                val id = destination.arguments?.getString("id")?.toLongOrNull()
                MealDetailScreen(uiState.allEntries.firstOrNull { it.id == id }, ::back)
            }
            composable(Screen.BarcodeLookup.route) {
                BarcodeLookupScreen(viewModel::lookupBarcode, { food -> selectedCatalogueFood = food; navigate(Screen.AddFood.route) },
                    { addFood(addFoodMeal) }, ::back)
            }
            composable(Screen.AddFood.route) {
                AddFoodScreen(initialMealType = addFoodMeal, searchResults = uiState.masterFoodSearchResults,
                    onSearchQueryChange = viewModel::setSearchQuery, onSave = viewModel::saveFoodEntry,
                    onNavigateBack = ::back, initialFood = selectedCatalogueFood, initialDate = uiState.selectedDate,
                    onSaved = { food ->
                        navController.navigate("meal_diary/${MealType.fromString(food.mealType).name}") {
                            popUpTo(Screen.AddFood.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    })
            }
            composable(Screen.History.route) { HistoryScreen(uiState,viewModel::setSearchQuery,viewModel::setMealTypeFilter,
                ::deleteFood, ::back) }
            composable(NakaRoutes.Coaches) { CoachRecommendationScreen(::back,{ navigate("coach_profile/$it") }) }
            composable(NakaRoutes.CoachProfile) { destination -> CoachProfileScreen(destination.arguments?.getString("id")?.toIntOrNull() ?: 0,::back,::openWorkout) }
            composable(Screen.Profile.route) {
                KcalFitProfileScreen(uiState,{ route -> if (route == Screen.WorkoutPlan.route) viewModel.generateWorkoutPlan(); navigate(route) }, {
                    viewModel.logout()
                    scope.launch { try { PasswordManager(context).signOut() } catch (_: Exception) { } }
                                        navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                })
            }
            composable(NakaRoutes.ProfileInfo) {
                ProfileInformationScreen(uiState,::back) { name,height,weight ->
                    viewModel.updateLocalProfile(name,height,weight)
                }
            }
            composable(NakaRoutes.Notifications) { NotificationSettingsScreen(::back, darkMode, onDarkModeChange) }
            composable(NakaRoutes.Devices) { DeviceConnectionScreen(::back) }
            composable(NakaRoutes.Premium) { PremiumScreen(::back,{ navigate(NakaRoutes.Subscription) }) }
            composable(NakaRoutes.Subscription) { SubscriptionScreen(::back) }
            composable(NakaRoutes.Settings) { SettingsScreen(::back,darkMode,onDarkModeChange,{ navigate(NakaRoutes.Notifications) }) }
            composable(NakaRoutes.Support) { SupportScreen(::back) }
            composable(Screen.Chatbot.route) {
                val busy by viewModel.coachBusy.collectAsState()
                val action by viewModel.coachAction.collectAsState()
                val error by viewModel.coachError.collectAsState()
                ChatbotScreen(uiState, viewModel::sendCoachQuery, busy, action, error, onAction = { target ->
                    viewModel.clearCoachAction()
                    target.date?.let(viewModel::setSelectedDate)
                    when(target.destination) {
                        CoachDestination.NUTRITION -> if(target.meal != null) openDiary(target.meal) else navigate(Screen.Nutrition.route)
                        CoachDestination.ADD_FOOD -> {
                            addFoodMeal = target.meal ?: MealType.BREAKFAST
                            selectedCatalogueFood = target.food
                            navigate(Screen.AddFood.route)
                        }
                        CoachDestination.TRACK -> navigate(Screen.Track.route)
                        CoachDestination.PROGRESS -> navigate(NakaRoutes.Progress)
                        CoachDestination.EXERCISES -> navigate(Screen.Exercise.route)
                        CoachDestination.HOME -> navigate(Screen.Home.route)
                    }
                }, onBack = ::back)
            }
            composable(Screen.CalorieSuggestion.route) {
                val result by viewModel.onboardingResult.collectAsState()
                result?.let { calc -> CalorieSuggestionScreen(calc.bmr,calc.tdee,calc.dailyCalorieTarget,uiState.targetProteinGrams,
                    uiState.targetCarbsGrams,uiState.targetFatGrams,uiState.userProfile,::updateGoal,
                    { viewModel.generateWorkoutPlan();navigate(Screen.WorkoutPlan.route) },{ navigate(Screen.Home.route) }) }
            }
            composable(Screen.WorkoutPlan.route) {
                val plan by viewModel.workoutPlan.collectAsState()
                WorkoutPlanScreen(plan,uiState.userProfile?.fitnessGoal ?: "LOSE_WEIGHT",uiState.userProfile?.exerciseDaysPerWeek ?: 3,
                    { navigate(Screen.Home.route) },viewModel::generateWorkoutPlan,::back)
            }
        }
        foodMutationError?.let { message -> androidx.compose.material3.AlertDialog(
            onDismissRequest = { foodMutationError = null }, title = { androidx.compose.material3.Text("Could not delete entry") },
            text = { androidx.compose.material3.Text(message) }, confirmButton = { androidx.compose.material3.TextButton({ foodMutationError = null }) { androidx.compose.material3.Text("OK") } }) }
        if (showChatbot) FloatingChatbotShortcut(
            onClick = { navigate(Screen.Chatbot.route) },
            modifier = Modifier.align(Alignment.TopStart).imePadding().padding(12.dp),
            positionX = chatX, positionY = chatY, onPositionChanged = { x, y -> chatX = x; chatY = y }
        )
        }
    }
}
