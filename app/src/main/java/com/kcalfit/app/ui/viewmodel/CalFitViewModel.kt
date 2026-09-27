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
import com.kcalfit.app.domain.calculator.ActivityLevel
import com.kcalfit.app.domain.calculator.BmrCalculator
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.calculator.MacroCalculator
import com.kcalfit.app.domain.calculator.NlpFoodParser
import com.kcalfit.app.domain.calculator.Sex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMealTypeFilter = MutableStateFlow<String?>(null)
    val selectedMealTypeFilter: StateFlow<String?> = _selectedMealTypeFilter.asStateFlow()

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
        }
    }

    // Sub-combine 1
    private val mainFlow1 = combine(
        userProfileFlow,
        _selectedDate,
        selectedDateEntriesFlow
    ) { user, date, foodEntries ->
        Triple(user, date, foodEntries)
    }

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

        val totalCals = foodEntries.sumOf { it.calories }
        val burnedCals = exerciseEntries.sumOf { it.caloriesBurned }
        val netCals = totalCals - burnedCals
        val remaining = goal - netCals
        val isOver = netCals > goal
        val excess = if (isOver) netCals - goal else 0

        val proteinSum = foodEntries.sumOf { it.proteinGrams }
        val carbsSum = foodEntries.sumOf { it.carbsGrams }
        val fatSum = foodEntries.sumOf { it.fatGrams }

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
    val uiState: StateFlow<CalFitUiState> = combine(
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalFitUiState()
    )

    // Actions
    fun setSelectedDate(date: String) { _selectedDate.value = date }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setMealTypeFilter(filter: String?) { _selectedMealTypeFilter.value = filter }

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
        targetWeightKg: Double, activityLevelStr: String, goalStr: String
    ) {
        viewModelScope.launch {
            val sex = if (sexStr.contains("FEMALE", ignoreCase = true)) Sex.FEMALE else Sex.MALE
            val activity = ActivityLevel.entries.firstOrNull { it.name.equals(activityLevelStr, ignoreCase = true) } ?: ActivityLevel.MODERATELY_ACTIVE
            val goal = FitnessGoal.entries.firstOrNull { it.name.equals(goalStr, ignoreCase = true) } ?: FitnessGoal.LOSE_WEIGHT

            val calc = BmrCalculator.computeFullProfile(weightKg, heightCm, age, sex, activity, goal)
            val macros = MacroCalculator.calculateMacros(calc.dailyCalorieTarget)

            val updatedUser = UserEntity(
                id = 1,
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
                dailyCalorieGoal = calc.dailyCalorieTarget,
                targetProteinGrams = macros.proteinGrams,
                targetCarbsGrams = macros.carbsGrams,
                targetFatGrams = macros.fatGrams
            )

            repository.saveUserProfile(updatedUser)
            repository.setCalorieGoal(calc.dailyCalorieTarget)
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
                    quantity = quantity.ifBlank { "1 serving" },
                    date = date
                )
            )
        }
    }

    fun deleteFoodEntry(entry: FoodEntry) {
        viewModelScope.launch { repository.deleteFoodEntry(entry) }
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

    fun startFasting(protocol: String, durationHours: Int) {
        viewModelScope.launch { repository.startFasting(protocol, durationHours) }
    }

    fun sendCoachQuery(userQuery: String) {
        viewModelScope.launch {
            repository.sendCoachMessage(userQuery, "USER")

            val currentCals = uiState.value.totalCalories
            val goalCals = uiState.value.dailyGoal
            val remainingCals = uiState.value.remainingCalories
            val protein = uiState.value.totalProteinGrams.toInt()

            val aiResponse = when {
                userQuery.contains("track", ignoreCase = true) -> "You have consumed $currentCals of your $goalCals kcal target today ($remainingCals kcal remaining). Your protein intake is $protein g."
                userQuery.contains("eat", ignoreCase = true) || userQuery.contains("suggest", ignoreCase = true) -> "With $remainingCals kcal remaining, a high-protein option like Grilled Chicken Salad, Paneer Tikka, or 2 Boiled Eggs with Chapati would fit great!"
                userQuery.contains("protein", ignoreCase = true) -> "You've logged $protein g of protein today. To reach your ${uiState.value.targetProteinGrams}g target, consider Greek Yogurt, Eggs, or Lentils!"
                else -> "Great progress on your daily log! Remember to stay hydrated and keep logging your meals to build your streak!"
            }

            repository.sendCoachMessage(aiResponse, "COACH")
        }
    }

    fun processVoiceInput(spokenText: String) {
        viewModelScope.launch {
            val parsedList = NlpFoodParser.parseVoiceInput(spokenText)
            for (parsed in parsedList) {
                addFoodEntry(
                    foodName = parsed.foodName,
                    brand = "Voice Log",
                    mealType = parsed.detectedMealType.displayName,
                    calories = parsed.estimatedCalories,
                    protein = parsed.estimatedProtein.toDouble(),
                    carbs = parsed.estimatedCarbs.toDouble(),
                    fat = parsed.estimatedFat.toDouble(),
                    quantity = parsed.estimatedQuantity,
                    date = _selectedDate.value
                )
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch { repository.seedSampleDataIfNeeded(force = true) }
    }

    fun logout() {
        viewModelScope.launch { repository.setLoggedIn(false) }
    }
}
