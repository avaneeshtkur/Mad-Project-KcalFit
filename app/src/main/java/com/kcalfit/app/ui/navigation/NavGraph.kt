package com.kcalfit.app.ui.navigation

import com.kcalfit.app.ui.reference.*
import android.content.Intent
import android.net.Uri
import com.kcalfit.app.domain.chatbot.CoachDestination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    darkMode: Boolean = false, onDarkModeChange: (Boolean) -> Unit = {}, modifier: Modifier = Modifier,
    authCallbackUri: Uri? = null, onAuthCallbackHandled: () -> Unit = {}) {
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
    var recoveryAccessToken by remember { mutableStateOf<String?>(null) }
    var recoveryRefreshToken by remember { mutableStateOf("") }
    var authCallbackPending by remember { mutableStateOf(authCallbackUri != null) }
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
    fun launchGoogle() {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.googleSignInUrl()))) }
        catch (_: Exception) { android.widget.Toast.makeText(context, "Could not open Google sign-in. Check that a browser is installed.", android.widget.Toast.LENGTH_LONG).show() }
    }

    LaunchedEffect(authCallbackUri) {
        val callback = authCallbackUri ?: return@LaunchedEffect
        authCallbackPending = true
        onAuthCallbackHandled()
        val fragment = callback.fragment.orEmpty()
        val tokenData = Uri.parse("calfit://callback/?$fragment")
        val access = tokenData.getQueryParameter("access_token")
            ?: callback.getQueryParameter("access_token").orEmpty()
        val refresh = tokenData.getQueryParameter("refresh_token")
            ?: callback.getQueryParameter("refresh_token").orEmpty()
        val callbackType = tokenData.getQueryParameter("type") ?: callback.getQueryParameter("type")
        val callbackError = tokenData.getQueryParameter("error_description")
            ?: callback.getQueryParameter("error_description")
            ?: tokenData.getQueryParameter("error")
            ?: callback.getQueryParameter("error")
        if (!callbackError.isNullOrBlank()) {
            android.widget.Toast.makeText(context, callbackError.replace('+', ' '), android.widget.Toast.LENGTH_LONG).show()
            authCallbackPending = false
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        } else if (access.isNotBlank() && refresh.isNotBlank() && callbackType == "recovery") {
            recoveryAccessToken = access
            recoveryRefreshToken = refresh
            authCallbackPending = false
            navController.navigate(Screen.ResetPassword.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        } else if (access.isNotBlank() && refresh.isNotBlank()) {
            viewModel.finishGoogleSignIn(access, refresh) { error, onboarded ->
                authCallbackPending = false
                if (error == null) navController.navigate(if (onboarded) Screen.Home.route else Screen.Onboarding.route) {
                    popUpTo(0) { inclusive = true }; launchSingleTop = true
                } else {
                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                    authCallbackPending = false
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                }
            }
        } else {
            android.widget.Toast.makeText(context, "The authentication link is incomplete or expired. Request a new link and try again.", android.widget.Toast.LENGTH_LONG).show()
            authCallbackPending = false
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        }
    }

    val currentRoute = entry?.destination?.route
    LaunchedEffect(authReady, authenticated, currentRoute) {
        val publicRoutes = setOf(Screen.Splash.route, Screen.Welcome.route, Screen.Login.route,
            Screen.Register.route, Screen.EmailVerification.route, Screen.ForgotPassword.route)
        if (authReady && !authenticated && !authCallbackPending && currentRoute != null && currentRoute !in publicRoutes) {
            navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        }
    }
    val bottomRoutes = Screen.bottomNavItems.map { it.route }.toSet()
    val showChatbot = currentRoute in setOf(
        Screen.Track.route, Screen.Insight.route,
        Screen.Profile.route, Screen.Exercise.route, NakaRoutes.Progress, NakaRoutes.HealthSummary,
        NakaRoutes.Coaches
    )
    Scaffold(bottomBar = { if (currentRoute in bottomRoutes) BottomNavigationBar(navController) },
        modifier = modifier) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        NavHost(navController, startDestination = Screen.Splash.route, modifier = Modifier.fillMaxSize()) {
            composable(Screen.Splash.route) {
                SplashScreen(uiState.userProfile, authReady && !authCallbackPending, { target -> navController.navigate(target) { popUpTo(Screen.Splash.route) { inclusive = true } } })
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
                }, onNavigateToRegister = { navigate(Screen.Register.route) }, onNavigateToForgotPassword = { navigate(Screen.ForgotPassword.route) }, errorMessage = error, isLoading = loading,
                    onGoogleSignIn = { launchGoogle() })
            }
            composable(Screen.Register.route) {
                var error by remember { mutableStateOf<String?>(null) }
                var loading by remember { mutableStateOf(false) }
                ReferenceSignup(onRegister = { name, email, password ->
                    loading = true; error = null
                    viewModel.registerUser(name,email,password) { result -> loading = false; if (result == null) { navigate(Screen.EmailVerification.route) } else error = result }
                }, onBack = { back() }, errorMessage = error, isLoading = loading,
                    onGoogle = { launchGoogle() })
            }
            composable(Screen.EmailVerification.route) {
                EmailVerificationScreen(pendingEmail.ifBlank { userPreferences.getUserEmail() }, viewModel::verifyEmailCode,
                    { navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } },
                    viewModel::resendVerificationCode, { navigate(Screen.Login.route) })
            }
            composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(viewModel::requestPasswordReset, ::back) }
            composable(Screen.ResetPassword.route) {
                val accessToken = recoveryAccessToken
                if (accessToken == null) NakaPage("Password reset", { navigate(Screen.Login.route) }) {
                    Text("Open the latest password-reset link from your email to continue.")
                } else ResetPasswordScreen(onSubmit = { password, result ->
                    viewModel.finishPasswordReset(accessToken, recoveryRefreshToken, password) { error, onboarded ->
                        result(error)
                        if (error == null) {
                            recoveryAccessToken = null
                            navController.navigate(if (onboarded) Screen.Home.route else Screen.Onboarding.route) {
                                popUpTo(0) { inclusive = true }; launchSingleTop = true
                            }
                        }
                    }
                }, onBack = { navigate(Screen.Login.route) })
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(uiState.userProfile?.name ?: "", { name, age, sex, height, weight, target, activity, goal, days ->
                    viewModel.completeOnboarding(name,age,sex,height,weight,target,activity,goal,days)
                    navigate(Screen.CalorieSuggestion.route)
                })
            }
            composable(Screen.Home.route) {
                LaunchedEffect(Unit) { viewModel.setSelectedDate(java.time.LocalDate.now().toString()) }
                val tracker = remember { com.kcalfit.app.data.health.StepTracking(context) }
                var steps by remember { mutableStateOf(tracker.count()) }
                DisposableEffect(tracker) {
                    val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> steps = tracker.count() }
                    tracker.prefs.registerOnSharedPreferenceChangeListener(listener)
                    onDispose { tracker.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
                }
                LaunchedEffect(Unit) { runCatching { tracker.sync() }; steps = tracker.count() }
                var premium by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { runCatching { com.kcalfit.app.data.premium.PremiumApi().entitlement() }.onSuccess { premium = it.premium } }
                ReferenceDashboard(uiState, ::navigate, ::updateGoal, premium = premium, steps = steps)

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
            composable("food_image_scan") { FoodImageScanScreen(::back, { food -> selectedCatalogueFood = food; navController.navigate(Screen.AddFood.route) { popUpTo(Screen.AddFood.route) { inclusive = true } } }, { navigate(NakaRoutes.Premium) }) }
            composable("health_connect") { HealthConnectionScreen(::back) }
            composable(Screen.Diary.route) {
                ReferenceDiary(uiState, ::back, viewModel::setSelectedDate, ::addFood, ::openMeal, ::deleteFood,
                    { navigate(Screen.Nutrition.route) }, viewModel::toggleFasting)
            }
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
                ReferenceNutrition(uiState, ::back, viewModel::setSelectedDate, { navigate(NakaRoutes.Settings) }, ::openMeal)
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
                AddFoodScreen(initialMealType = addFoodMeal,
                    onSearchQueryChange = viewModel::setSearchQuery, onSave = viewModel::saveFoodEntry,
                    onNavigateBack = ::back, initialFood = selectedCatalogueFood, initialDate = uiState.selectedDate,
                    foodSearchState = uiState.foodSearchState, onRetrySearch = viewModel::retryFoodSearch, onScanFood = { navigate("food_image_scan") },
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
            composable(NakaRoutes.Premium) { PremiumSubscriptionScreen(::back) }
            composable(NakaRoutes.Subscription) { PremiumSubscriptionScreen(::back) }
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
                if (result != null || uiState.userProfile?.isOnboarded == true) ReferenceAccountCreated(result?.dailyCalorieTarget ?: uiState.dailyGoal) { navigate(Screen.Home.route) }
                else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator() }
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
