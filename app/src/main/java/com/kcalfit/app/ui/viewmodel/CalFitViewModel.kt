package com.kcalfit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalfit.app.data.model.ActivityPostEntity
import com.kcalfit.app.data.model.CoachMessageEntity
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.data.model.FastingSessionEntity
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.FriendEntity
import com.kcalfit.app.data.model.GroceryItemEntity
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.data.model.RecipeEntity
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.model.WeightEntry
import com.kcalfit.app.data.repository.FoodRepository
import com.kcalfit.app.data.repository.SupabaseAuthService
import com.kcalfit.app.CalFitApplication
import com.kcalfit.app.domain.calculator.ActivityLevel
import com.kcalfit.app.domain.calculator.BmrCalculator
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.calculator.MacroCalculator
import com.kcalfit.app.domain.calculator.Sex
import com.kcalfit.app.domain.calculator.CalorieCalculationResult
import com.kcalfit.app.domain.calculator.WorkoutPlanGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.CancellationException
import com.kcalfit.app.domain.chatbot.*
import com.kcalfit.app.domain.calculator.FoodDiaryRules
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

data class CalFitUiState(
    val selectedDate: String = getTodayDateString(),
    val userProfile: UserEntity? = null,
    val dailyGoal: Int = 2100,
    val targetProteinGrams: Int = 150,
    val targetCarbsGrams: Int = 210,
    val targetFatGrams: Int = 60,
    val selectedDateEntries: List<FoodEntry> = emptyList(),
    val allEntries: List<FoodEntry> = emptyList(),
    val masterFoodSearchResults: List<FoodItemEntity> = emptyList(),
    val totalCalories: Int = 0,
    val remainingCalories: Int = 2100,
    val caloriesBurned: Int = 0,
    val netCalories: Int = 0,
    val isOverGoal: Boolean = false,
    val excessCalories: Int = 0,
    val totalProteinGrams: Double = 0.0,
    val totalCarbsGrams: Double = 0.0,
    val totalFatGrams: Double = 0.0,
    val totalWaterMl: Int = 0,
    val latestWeightKg: Double = 72.0,
    val activeFastingSession: FastingSessionEntity? = null,
    val mealBreakdown: Map<String, Int> = emptyMap(),
    val exerciseEntries: List<ExerciseEntry> = emptyList(),
    val allExerciseEntries: List<ExerciseEntry> = emptyList(),
    val recipesList: List<RecipeEntity> = emptyList(),
    val coachMessages: List<CoachMessageEntity> = emptyList(),
    val friendsList: List<FriendEntity> = emptyList(),
    val activityPosts: List<ActivityPostEntity> = emptyList(),
    val groceryList: List<GroceryItemEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedMealTypeFilter: String? = null
)

fun getTodayDateString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalFitViewModel(
    private val repository: FoodRepository
) : ViewModel() {
    private val _onboardingResult = MutableStateFlow<CalorieCalculationResult?>(null)
    val onboardingResult: StateFlow<CalorieCalculationResult?> = _onboardingResult.asStateFlow()
    private val _workoutPlan = MutableStateFlow<List<com.kcalfit.app.domain.calculator.WorkoutDayPlan>>(emptyList())
    val workoutPlan: StateFlow<List<com.kcalfit.app.domain.calculator.WorkoutDayPlan>> = _workoutPlan.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMealTypeFilter = MutableStateFlow<String?>(null)
    val selectedMealTypeFilter: StateFlow<String?> = _selectedMealTypeFilter.asStateFlow()

    private val authService = SupabaseAuthService()
    private val preferences get() = CalFitApplication.instance.userPreferences
    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()
    private val _authenticated = MutableStateFlow(false)
    val authenticated: StateFlow<Boolean> = _authenticated.asStateFlow()
    private val _pendingEmail = MutableStateFlow("")
    val pendingEmail: StateFlow<String> = _pendingEmail.asStateFlow()

    // Database flows
    val userProfileFlow = repository.getUserProfileFlow()
    val selectedDateEntriesFlow = _selectedDate.flatMapLatest { date -> repository.getEntriesForDate(date) }
    val allEntriesFlow = repository.getAllEntries()
    val exerciseEntriesFlow = _selectedDate.flatMapLatest { date -> repository.getExerciseEntriesForDate(date) }
    val waterIntakeFlow = _selectedDate.flatMapLatest { date -> repository.getTotalWaterForDate(date) }
    val latestWeightFlow = repository.getLatestWeight()
    val activeFastingFlow = repository.getActiveFastingSession()
    val masterFoodSearchFlow = _searchQuery.flatMapLatest { query -> repository.searchMasterFoods(query) }
    val coachMessagesFlow = repository.getCoachMessages()
    val recipesFlow = repository.getAllRecipes()
    val friendsFlow = repository.getFriendsList()
    val activityPostsFlow = repository.getActivityPosts()
    val groceryFlow = repository.getGroceryItems()

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded()
            val refreshToken = preferences.getRefreshToken()
            if (refreshToken.isNotBlank()) {
                try {
                    val session = authService.refresh(refreshToken)
                    acceptSession(session)
                } catch (e: Exception) {
                    if (e is SupabaseAuthService.AuthHttpException && e.status in listOf(400, 401, 403)) {
                        preferences.clearAuthSession()
                        repository.setLoggedIn(false)
                    }
                    // A network failure keeps the existing local session for offline diary use.
                }
            } else {
                repository.setLoggedIn(false)
            }
            _authenticated.value = repository.getUserProfile()?.isLoggedIn == true
            _authReady.value = true
        }
    }

    // Sub-combine 1
    private val mainFlow1 = combine(
        userProfileFlow,
        _selectedDate.flatMapLatest { date -> repository.getEntriesForDate(date).map { date to it } }
    ) { user, datedEntries -> Triple(user, datedEntries.first, datedEntries.second) }

    // Sub-combine 2
    private val mainFlow2 = combine(
        exerciseEntriesFlow,
        waterIntakeFlow,
        latestWeightFlow
    ) { exerciseEntries, water, weight ->
        Triple(exerciseEntries, water, weight)
    }

    // Combine into primary state
    private val primaryStateFlow = combine(
        mainFlow1,
        mainFlow2
    ) { (user, date, foodEntries), (exerciseEntries, water, weight) ->

        val goal = user?.dailyCalorieGoal ?: 2100
        val targetProtein = user?.targetProteinGrams ?: 150
        val targetCarbs = user?.targetCarbsGrams ?: 210
        val targetFat = user?.targetFatGrams ?: 60

        val totals = FoodDiaryRules.totals(foodEntries)
        val totalCals = totals.calories
        val burnedCals = exerciseEntries.sumOf { it.caloriesBurned }
        val netCals = totalCals - burnedCals
        val remaining = goal - totalCals
        val isOver = totalCals > goal
        val excess = if (isOver) totalCals - goal else 0

        val proteinSum = totals.protein
        val carbsSum = totals.carbs
        val fatSum = totals.fat

        val breakdown = mutableMapOf(
            MealType.BREAKFAST.displayName to 0,
            MealType.LUNCH.displayName to 0,
            MealType.DINNER.displayName to 0,
            MealType.SNACK.displayName to 0
        )

        foodEntries.forEach { entry ->
            val mealName = MealType.fromString(entry.mealType).displayName
            breakdown[mealName] = (breakdown[mealName] ?: 0) + entry.calories
        }

        CalFitUiState(
            selectedDate = date,
            userProfile = user,
            dailyGoal = goal,
            targetProteinGrams = targetProtein,
            targetCarbsGrams = targetCarbs,
            targetFatGrams = targetFat,
            selectedDateEntries = foodEntries,
            totalCalories = totalCals,
            remainingCalories = if (remaining < 0) 0 else remaining,
            caloriesBurned = burnedCals,
            netCalories = netCals,
            isOverGoal = isOver,
            excessCalories = excess,
            totalProteinGrams = proteinSum,
            totalCarbsGrams = carbsSum,
            totalFatGrams = fatSum,
            totalWaterMl = water ?: 0,
            latestWeightKg = weight?.weightKg ?: (user?.weightKg ?: 72.0),
            mealBreakdown = breakdown,
            exerciseEntries = exerciseEntries
        )
    }

    // Final UI State flow
    private val enrichedState = combine(
        primaryStateFlow,
        allEntriesFlow,
        masterFoodSearchFlow,
        coachMessagesFlow,
        recipesFlow
    ) { state, allEntries, searchResults, coachMsgs, recipes ->
        state.copy(
            allEntries = allEntries,
            masterFoodSearchResults = searchResults,
            coachMessages = coachMsgs,
            recipesList = recipes
        )
    }
    val uiState: StateFlow<CalFitUiState> = combine(enrichedState, repository.getAllExerciseEntries(), _searchQuery, _selectedMealTypeFilter) { state, workouts, query, filter ->
        state.copy(allExerciseEntries = workouts, searchQuery = query, selectedMealTypeFilter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalFitUiState()
    )

    // Actions
    fun setSelectedDate(date: String) { _selectedDate.value = date }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setMealTypeFilter(filter: String?) { _selectedMealTypeFilter.value = filter }

    fun updateLocalProfile(name: String? = null, height: Double? = null, weight: Double? = null) {
        viewModelScope.launch {
            val user = repository.getUserProfile() ?: return@launch
            repository.saveUserProfile(user.copy(name = name ?: user.name, heightCm = height ?: user.heightCm, weightKg = weight ?: user.weightKg))
            if (weight != null) repository.addWeight(weight, _selectedDate.value, "Profile update")
        }
    }

    fun lookupBarcode(barcode: String, onResult: (FoodItemEntity?) -> Unit) {
        viewModelScope.launch { onResult(repository.findFoodByBarcode(barcode.trim())) }
    }

    // Auth & Verification
    fun registerUser(name: String, email: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                authService.register(name, email, password)
                _pendingEmail.value = email
                preferences.setUserEmail(email)
                onResult(null)
            } catch (e: Exception) { onResult(e.message ?: "Could not create account") }
        }
    }

    fun verifyEmailCode(inputCode: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val email = _pendingEmail.value.ifBlank { preferences.getUserEmail() }
                if (email.isBlank()) throw IllegalStateException("Please register again to request a code.")
                acceptSession(authService.verify(email, inputCode))
                onResult(null)
            } catch (e: Exception) { onResult(e.message ?: "Invalid or expired code") }
        }
    }

    fun loginUser(email: String, password: String, onResult: (String?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val onboarded = acceptSession(authService.login(email, password))
                onResult(null, onboarded)
            } catch (e: Exception) { onResult(e.message ?: "Could not log in", false) }
        }
    }

    fun resendVerificationCode(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                authService.resend(_pendingEmail.value.ifBlank { preferences.getUserEmail() })
                onResult(null)
            } catch (e: Exception) { onResult(e.message ?: "Could not resend code") }
        }
    }

    fun requestPasswordReset(email: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try { authService.requestPasswordReset(email); onResult(null) }
            catch (e: Exception) { onResult(e.message ?: "Could not send reset email") }
        }
    }

    private suspend fun acceptSession(session: SupabaseAuthService.Session): Boolean {
        coachJob?.cancelAndJoin()
        coach.reset()
        _coachAction.value = null
        val previous = repository.getUserProfile()
        if (previous != null && previous.firebaseUid != session.userId) {
            // One local profile/log at a time: never show another account's entries.
            withContext(Dispatchers.IO) { CalFitApplication.instance.database.clearAllTables() }
            repository.seedSampleDataIfNeeded()
        }
        val current = repository.getUserProfile()
        val remote = session.metadata
        val updated = (current ?: UserEntity()).copy(
            name = session.name.ifBlank { current?.name ?: "Fitness User" },
            email = session.email,
            passwordHash = "",
            firebaseUid = session.userId,
            isEmailVerified = true,
            isLoggedIn = true,
            verificationCode = "",
            verificationCodeTimestamp = 0,
            isOnboarded = current?.isOnboarded ?: remote.optBoolean("is_onboarded", false),
            age = current?.age ?: remote.optInt("age", 25),
            sex = current?.sex ?: remote.optString("sex", "MALE"),
            heightCm = current?.heightCm ?: remote.optDouble("height_cm", 175.0),
            weightKg = current?.weightKg ?: remote.optDouble("weight_kg", 70.0),
            targetWeightKg = current?.targetWeightKg ?: remote.optDouble("target_weight_kg", 65.0),
            activityLevel = current?.activityLevel ?: remote.optString("activity_level", "MODERATELY_ACTIVE"),
            fitnessGoal = current?.fitnessGoal ?: remote.optString("fitness_goal", "LOSE_WEIGHT"),
            exerciseDaysPerWeek = current?.exerciseDaysPerWeek ?: remote.optInt("exercise_days_per_week", 3),
            dailyCalorieGoal = current?.dailyCalorieGoal ?: remote.optInt("daily_calorie_goal", 2000)
        )
        repository.saveUserProfile(updated)
        preferences.saveAuthSession(session.userId, session.refreshToken)
        preferences.setUserEmail(session.email)
        _authenticated.value = true
        return updated.isOnboarded
    }

    fun updateCalorieGoal(newGoal: Int) {
        viewModelScope.launch {
            repository.setCalorieGoal(newGoal)
            val user = repository.getUserProfile()
            if (user != null) {
                repository.saveUserProfile(user.copy(dailyCalorieGoal = newGoal))
            }
        }
    }

    fun completeOnboarding(
        name: String, age: Int, sexStr: String, heightCm: Double, weightKg: Double,
        targetWeightKg: Double, activityLevelStr: String, goalStr: String, exerciseDaysPerWeek: Int
    ) {
        viewModelScope.launch {
            val sex = if (sexStr.contains("FEMALE", ignoreCase = true)) Sex.FEMALE else Sex.MALE
            val activity = ActivityLevel.entries.firstOrNull { it.name.equals(activityLevelStr, ignoreCase = true) } ?: ActivityLevel.MODERATELY_ACTIVE
            val goal = FitnessGoal.entries.firstOrNull { it.name.equals(goalStr, ignoreCase = true) } ?: FitnessGoal.LOSE_WEIGHT

            val calc = BmrCalculator.computeFullProfile(weightKg, heightCm, age, sex, activity, goal)
            val macros = MacroCalculator.calculateMacros(calc.dailyCalorieTarget)

            val existing = repository.getUserProfile() ?: return@launch
            val updatedUser = existing.copy(
                name = name,
                isLoggedIn = true,
                isOnboarded = true,
                age = age,
                sex = sex.name,
                heightCm = heightCm,
                weightKg = weightKg,
                targetWeightKg = targetWeightKg,
                activityLevel = activity.name,
                fitnessGoal = goal.name,
                exerciseDaysPerWeek = exerciseDaysPerWeek,
                dailyCalorieGoal = calc.dailyCalorieTarget,
                targetProteinGrams = macros.proteinGrams,
                targetCarbsGrams = macros.carbsGrams,
                targetFatGrams = macros.fatGrams
            )

            repository.saveUserProfile(updatedUser)
            repository.setCalorieGoal(calc.dailyCalorieTarget)
            _onboardingResult.value = calc
            try {
                authService.updateProfile(
                    preferences.getRefreshToken(),
                    JSONObject()
                        .put("name", name)
                        .put("is_onboarded", true)
                        .put("age", age)
                        .put("sex", sex.name)
                        .put("height_cm", heightCm)
                        .put("weight_kg", weightKg)
                        .put("target_weight_kg", targetWeightKg)
                        .put("activity_level", activity.name)
                        .put("fitness_goal", goal.name)
                        .put("exercise_days_per_week", exerciseDaysPerWeek)
                        .put("daily_calorie_goal", calc.dailyCalorieTarget),
                    onRefreshed = { preferences.saveAuthSession(it.userId, it.refreshToken) }
                )
            } catch (_: Exception) {
                // Local onboarding remains usable offline; sync can retry on the next edit.
            }
        }
    }

    fun generateWorkoutPlan() {
        viewModelScope.launch {
            repository.getUserProfile()?.let { user ->
                val goal = FitnessGoal.entries.firstOrNull { it.name == user.fitnessGoal } ?: FitnessGoal.LOSE_WEIGHT
                val activity = ActivityLevel.entries.firstOrNull { it.name == user.activityLevel } ?: ActivityLevel.MODERATELY_ACTIVE
                _workoutPlan.value = WorkoutPlanGenerator.generatePlan(goal, user.exerciseDaysPerWeek, activity)
            }
        }
    }

    fun addFoodEntry(
        foodName: String,
        brand: String = "Generic",
        mealType: String,
        calories: Int,
        protein: Double = 0.0,
        carbs: Double = 0.0,
        fat: Double = 0.0,
        fiber: Double = 0.0,
        sugar: Double = 0.0,
        sodium: Double = 0.0,
        quantity: String = "1 serving",
        date: String
    ) {
        viewModelScope.launch {
            repository.insertFoodEntry(
                FoodEntry(
                    foodName = foodName.trim(),
                    brand = brand.ifBlank { "Generic" },
                    mealType = mealType,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    fiberGrams = fiber,
                    sugarGrams = sugar,
                    sodiumMg = sodium,
                    quantity = quantity.ifBlank { "1 serving" },
                    date = date
                )
            )
        }
    }

    fun deleteFoodEntry(entry: FoodEntry) {
        viewModelScope.launch { repository.deleteFoodEntry(entry) }
    }

    fun saveFoodEntry(entry: FoodEntry, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try { repository.insertFoodEntry(entry); _selectedDate.value = entry.date; onResult(null) }
            catch (e: Exception) { onResult(e.message ?: "Food could not be saved") }
        }
    }

    fun removeFoodEntry(entry: FoodEntry, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try { repository.deleteFoodEntry(entry); onResult(null) }
            catch (e: Exception) { onResult(e.message ?: "Entry could not be deleted") }
        }
    }

    fun addWaterIntake(amountMl: Int) {
        viewModelScope.launch { repository.addWater(amountMl, _selectedDate.value) }
    }

    fun logWeight(weightKg: Double, notes: String = "") {
        viewModelScope.launch { repository.addWeight(weightKg, _selectedDate.value, notes) }
    }

    fun logExercise(name: String, category: String, durationMinutes: Int, caloriesBurned: Int) {
        viewModelScope.launch {
            repository.insertExerciseEntry(
                ExerciseEntry(
                    exerciseName = name,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    date = _selectedDate.value
                )
            )
        }
    }

    fun deleteExerciseEntry(entry: ExerciseEntry) {
        viewModelScope.launch {
            repository.deleteExerciseEntry(entry)
        }
    }

    fun startFasting(protocol: String, durationHours: Int) {
        viewModelScope.launch { repository.startFasting(protocol, durationHours) }
    }

    private var coachJob: Job? = null
    private val coach = FitnessChatbot()
    private val coachResponder = CoachResponder()
    private val _coachBusy = MutableStateFlow(false)
    val coachBusy = _coachBusy.asStateFlow()
    private val _coachAction = MutableStateFlow<CoachAction?>(null)
    val coachAction = _coachAction.asStateFlow()
    private val _coachError = MutableStateFlow<String?>(null)
    val coachError = _coachError.asStateFlow()
    fun clearCoachAction() { _coachAction.value = null }

    fun sendCoachQuery(userQuery: String) {
        if (userQuery.isBlank() || _coachBusy.value || !_authenticated.value) return
        _coachBusy.value = true
        _coachError.value = null
        _coachAction.value = null
        coachJob = viewModelScope.launch {
            try {
                val query = userQuery.trim().take(2000)
                repository.sendCoachMessage(query, "USER")
                val data = CoachSnapshot(repository.getAllEntries().first(), repository.searchMasterFoods("").first(),
                    uiState.value.dailyGoal, _selectedDate.value, repository.getAllExerciseEntries().first().map { it.date },
                    uiState.value.userProfile?.exerciseDaysPerWeek ?: 0, uiState.value.targetProteinGrams,
                    uiState.value.targetCarbsGrams, uiState.value.targetFatGrams)
                val (request, localReply) = withContext(Dispatchers.Default) { coach.answer(query, data) }
                val reply = coachResponder.respond(query, localReply,
                    request.intent in setOf(CoachIntent.WORKOUT, CoachIntent.TECHNIQUE))
                repository.sendCoachMessage(reply.message, "COACH")
                _coachAction.value = reply.action
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _coachError.value = "Couldn't read or save this conversation. Please try again." }
            finally { _coachBusy.value = false }
        }
    }

    fun logout() {
        coachJob?.cancel()
        coach.reset()
        _coachAction.value = null
        val token = preferences.getRefreshToken()
        _authenticated.value = false
        preferences.clearSession()
        viewModelScope.launch {
            repository.setLoggedIn(false)
            try { authService.signOut(token) } catch (_: Exception) { /* Local session is already cleared. */ }
        }
    }
}
