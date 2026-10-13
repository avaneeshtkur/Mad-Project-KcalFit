package com.kcalfit.app.ui.navigation

import android.net.Uri

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
    val realState by viewModel.uiState.collectAsState()
    val authReady by viewModel.authReady.collectAsState()
    val pendingEmail by viewModel.pendingEmail.collectAsState()
    val entry by navController.currentBackStackEntryAsState()
    var demo by rememberSaveable { mutableStateOf(false) }
    var demoName by rememberSaveable { mutableStateOf("Anna Santos") }
    var demoHeight by rememberSaveable { mutableDoubleStateOf(170.0) }
    var demoWeight by rememberSaveable { mutableDoubleStateOf(64.4) }
    var demoWater by rememberSaveable { mutableIntStateOf(1250) }
    var demoGoal by rememberSaveable { mutableIntStateOf(2100) }
    var demoMeals by remember { mutableStateOf(nakaSampleMeals) }
    var demoExercises by remember { mutableStateOf(emptyList<ExerciseEntry>()) }
    var addFoodMeal by remember { mutableStateOf(MealType.BREAKFAST) }
    var selectedCatalogueFood by remember { mutableStateOf<FoodItemEntity?>(null) }
    val dailyDemoMeals = demoMeals.filter { it.date == realState.selectedDate }
    val uiState = if (demo) realState.copy(
        userProfile = UserEntity(name = demoName, email = "Preview account", heightCm = demoHeight, weightKg = demoWeight),
        allEntries = demoMeals, selectedDateEntries = dailyDemoMeals, totalCalories = dailyDemoMeals.sumOf { it.calories },
        totalProteinGrams = dailyDemoMeals.sumOf { it.proteinGrams }, totalCarbsGrams = dailyDemoMeals.sumOf { it.carbsGrams },
        totalFatGrams = dailyDemoMeals.sumOf { it.fatGrams }, dailyGoal = demoGoal, latestWeightKg = demoWeight,
        totalWaterMl = demoWater, exerciseEntries = demoExercises,
        caloriesBurned = demoExercises.sumOf { it.caloriesBurned },
        mealBreakdown = MealType.entries.associate { meal -> meal.displayName to dailyDemoMeals.filter { MealType.fromString(it.mealType) == meal }.sumOf { it.calories } }
    ) else realState

    fun navigate(route: String) { navController.navigate(route) { launchSingleTop = true } }
    fun back() { navController.popBackStack() }
    fun openMeal(food: FoodEntry) { navigate("meal_detail/${food.id}") }
    fun openWorkout(id: String) { navigate("workout_detail/$id") }
    fun addFood(meal: MealType) { selectedCatalogueFood=null;addFoodMeal = meal; navigate(Screen.AddFood.route) }
    fun selectFood(food: FoodItemEntity) { selectedCatalogueFood=food;addFoodMeal=MealType.BREAKFAST;navigate(Screen.AddFood.route) }
    fun updateMeasurements(height: Double?, weight: Double?) {
        if (demo) { if (height != null) demoHeight = height; if (weight != null) demoWeight = weight }
        else viewModel.updateLocalProfile(height = height, weight = weight)
    }
    fun addWater(amount: Int) { if (demo) demoWater += amount else viewModel.addWaterIntake(amount) }
    fun logWeight(weight: Double) { if (demo) demoWeight = weight else viewModel.logWeight(weight) }
    fun logExercise(name: String, category: String, duration: Int, calories: Int) {
        if (demo) demoExercises = demoExercises + ExerciseEntry(exerciseName = name, category = category,
            durationMinutes = duration, caloriesBurned = calories, date = realState.selectedDate)
        else viewModel.logExercise(name, category, duration, calories)
    }
    fun updateGoal(goal: Int) { if (demo) demoGoal = goal else viewModel.updateCalorieGoal(goal) }

    val currentRoute = entry?.destination?.route
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
                SplashScreen(realState.userProfile, authReady, { target -> navController.navigate(target) { popUpTo(Screen.Splash.route) { inclusive = true } } })
            }
            composable(Screen.Welcome.route) {
                WelcomeScreen(onNavigateToLogin = { navigate(Screen.Login.route) }, onNavigateToRegister = { navigate(Screen.Register.route) },
                    onExplore = { demo = true; navigate(Screen.Home.route) })
            }
            composable(Screen.Login.route) {
                var error by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(false) }
                LoginScreen(onLoginSuccess = { email, password ->
                    loading = true; error = null
                    viewModel.loginUser(email, password) { result, onboarded ->
                        loading = false
                        if (result == null) { demo = false; navController.navigate(if (onboarded) Screen.Home.route else Screen.Onboarding.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } }
                        else error = result
                    }
                }, onNavigateToRegister = { navigate(Screen.Register.route) }, onNavigateToForgotPassword = { navigate(Screen.ForgotPassword.route) }, errorMessage = error, isLoading = loading)
            }
            composable(Screen.Register.route) {
                var error by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(false) }
                RegisterScreen(onRegisterSuccess = { name, email, password ->
                    loading = true; error = null
                    viewModel.registerUser(name,email,password) { result -> loading = false; if (result == null) { demo = false; navigate(Screen.EmailVerification.route) } else error = result }
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
                    if (demo) { demoName = name; demoHeight = height; demoWeight = weight; navigate(Screen.Home.route) }
                    else { viewModel.completeOnboarding(name,age,sex,height,weight,target,activity,goal,days); navigate(Screen.CalorieSuggestion.route) }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(uiState, { addFood(it ?: MealType.BREAKFAST) }, { food -> if (demo) demoMeals = demoMeals - food else viewModel.deleteFoodEntry(food) },
                    ::updateGoal, darkMode, onDarkModeChange, { navigate(Screen.Exercise.route) }, { navigate(Screen.Nutrition.route) },
                    { navigate(Screen.AddGoals.route) }, { navigate(Screen.Profile.route) }, { navigate(Screen.Track.route) },
                    { navigate(NakaRoutes.Notifications) }, ::openWorkout, demo)
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
                { exercise -> if (demo) demoExercises = demoExercises - exercise else viewModel.deleteExerciseEntry(exercise) }, ::back) }
            composable(Screen.Track.route) {
                TrackScreen(uiState, if (demo) "preview" else userPreferences.getAuthUserId(), demo,
                    { title,activity -> navigate("daily_goal/${Uri.encode(title)}/${Uri.encode(activity)}") }, { navigate(Screen.AddGoals.route) }, { navigate(NakaRoutes.Progress) },
                    { navigate(NakaRoutes.HealthSummary) }, { navigate(NakaRoutes.Sleep) }, { navigate(NakaRoutes.Period) }, { navigate(Screen.Water.route) })
            }
            composable(Screen.AddGoals.route) { AddGoalsScreen(if (demo) "preview" else userPreferences.getAuthUserId(), ::back) }
            composable(NakaRoutes.DailyGoal) { destination ->
                val title=destination.arguments?.getString("title") ?: "Morning Run"
                val activity=destination.arguments?.getString("activity") ?: "Running"
                DailyGoalScreen(title,activity,if(demo) "preview" else userPreferences.getAuthUserId(),::back, {
                    when (activity) { "Yoga" -> openWorkout("yoga"); "Weights" -> openWorkout("weights"); "Running" -> openWorkout("run"); else -> navigate("workout_session/activity_${Uri.encode(activity)}") }
                })
            }
            composable(NakaRoutes.Progress) { ProgressScreen(uiState,demo,::back,::updateMeasurements,{ navigate(NakaRoutes.HealthLog) }) }
            composable(NakaRoutes.HealthSummary) { HealthSummaryScreen(uiState,demo,::back,{ navigate(NakaRoutes.HealthLog) }) }
            composable(NakaRoutes.HealthLog) { HealthLogScreen(::back,::logWeight,::addWater) }
            composable(NakaRoutes.Sleep) { SleepScreen(::back) }
            composable(NakaRoutes.Period) { PeriodScreen(::back) }
            composable(Screen.Insight.route) { InsightScreen(uiState,demo,{ navigate(Screen.Track.route) },{ navigate(NakaRoutes.Progress) }) }
            composable(Screen.Water.route) { WaterScreen(uiState,::addWater,::back) }
            composable(Screen.Weight.route) { WeightScreen(uiState,::logWeight,::back) }
            composable(Screen.Nutrition.route) {
                NutritionMenuScreen(uiState,demo,{ navigate(NakaRoutes.Breakfast) },::addFood,{ navigate(NakaRoutes.Search) },{ navigate(NakaRoutes.Suggestions) },::openMeal)
            }
            composable(NakaRoutes.Breakfast) {
                NutritionScreen(uiState,viewModel::setSelectedDate,{ food -> if (demo) demoMeals = demoMeals - food else viewModel.deleteFoodEntry(food) },
                    { addFood(MealType.BREAKFAST) }, { navigate(Screen.BarcodeLookup.route) }, { navigate(NakaRoutes.MealScan) }, ::back, ::openMeal)
            }
            composable(NakaRoutes.Suggestions) { SuggestionsScreen(::back,::openMeal) }
            composable(NakaRoutes.Search) { SearchScreen(uiState,::back,viewModel::setSearchQuery,::openMeal,::openWorkout,{ addFood(MealType.BREAKFAST) },::selectFood) }
            composable(Screen.FoodSearch.route) { SearchScreen(uiState,::back,viewModel::setSearchQuery,::openMeal,::openWorkout,{ addFood(MealType.BREAKFAST) },::selectFood) }
            composable(NakaRoutes.MealScan) { MealScanPreviewScreen(::back,{ openMeal(nakaSampleMeals.last()) }) }
            composable(Screen.MealDetail.route) { destination ->
                val id = destination.arguments?.getString("id")?.toLongOrNull()
                MealDetailScreen((uiState.allEntries + nakaSampleMeals).firstOrNull { it.id == id },::back)
            }
            composable(Screen.BarcodeLookup.route) {
                BarcodeLookupScreen(viewModel::lookupBarcode, { food ->
                    if (demo) demoMeals = demoMeals + FoodEntry(id = System.currentTimeMillis(),foodName=food.name,brand=food.brand,mealType="Breakfast",
                        calories=food.calories,proteinGrams=food.proteinGrams,carbsGrams=food.carbsGrams,fatGrams=food.fatGrams,date=uiState.selectedDate)
                    else viewModel.addFoodEntry(foodName=food.name,brand=food.brand,mealType="Breakfast",calories=food.calories,
                        protein=food.proteinGrams,carbs=food.carbsGrams,fat=food.fatGrams,date=uiState.selectedDate)
                    back()
                },{ addFood(MealType.BREAKFAST) },::back)
            }
            composable(Screen.AddFood.route) {
                AddFoodScreen(initialMealType=addFoodMeal,searchResults=uiState.masterFoodSearchResults,onSearchQueryChange=viewModel::setSearchQuery,
                    onAddFoodSubmit={ name,meal,calories,protein,carbs,fat,fiber,sugar,sodium,quantity,date ->
                        if (demo) demoMeals = demoMeals + FoodEntry(id=System.currentTimeMillis(),foodName=name,mealType=meal,calories=calories,
                            proteinGrams=protein,carbsGrams=carbs,fatGrams=fat,fiberGrams=fiber,sugarGrams=sugar,sodiumMg=sodium,quantity=quantity,date=date)
                        else viewModel.addFoodEntry(foodName=name,mealType=meal,calories=calories,protein=protein,carbs=carbs,fat=fat,fiber=fiber,sugar=sugar,sodium=sodium,quantity=quantity,date=date)
                        back()
                    },onNavigateBack=::back,initialFood=selectedCatalogueFood,initialDate=uiState.selectedDate)
            }
            composable(Screen.History.route) { HistoryScreen(uiState,viewModel::setSearchQuery,viewModel::setMealTypeFilter,
                { food -> if (demo) demoMeals = demoMeals - food else viewModel.deleteFoodEntry(food) }, { if (demo) demoMeals = nakaSampleMeals else viewModel.seedSampleData() }) }
            composable(NakaRoutes.Coaches) { CoachRecommendationScreen(::back,{ navigate("coach_profile/$it") }) }
            composable(NakaRoutes.CoachProfile) { destination -> CoachProfileScreen(destination.arguments?.getString("id")?.toIntOrNull() ?: 0,::back,::openWorkout) }
            composable(Screen.Profile.route) {
                KcalFitProfileScreen(uiState,demo,{ route -> if (route == Screen.WorkoutPlan.route) viewModel.generateWorkoutPlan(); navigate(route) }, {
                    if (!demo) viewModel.logout()
                    demo = false
                    navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                })
            }
            composable(NakaRoutes.ProfileInfo) {
                ProfileInformationScreen(uiState,demo,::back) { name,height,weight ->
                    if (demo) { demoName=name;demoHeight=height;demoWeight=weight } else viewModel.updateLocalProfile(name,height,weight)
                }
            }
            composable(NakaRoutes.Notifications) { NotificationSettingsScreen(::back, darkMode, onDarkModeChange) }
            composable(NakaRoutes.Devices) { DeviceConnectionScreen(::back) }
            composable(NakaRoutes.Premium) { PremiumScreen(::back,{ navigate(NakaRoutes.Subscription) }) }
            composable(NakaRoutes.Subscription) { SubscriptionScreen(::back) }
            composable(NakaRoutes.Settings) { SettingsScreen(::back,darkMode,onDarkModeChange,{ navigate(NakaRoutes.Notifications) }) }
            composable(NakaRoutes.Support) { SupportScreen(::back) }
            composable(Screen.Chatbot.route) { ChatbotScreen(uiState,viewModel::sendCoachQuery) }
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
        if (showChatbot) FloatingChatbotShortcut(
            onClick = { navigate(Screen.Chatbot.route) },
            modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 12.dp)
        )
        }
    }
}
