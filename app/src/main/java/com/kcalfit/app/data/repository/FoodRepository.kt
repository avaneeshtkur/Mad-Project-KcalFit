package com.kcalfit.app.data.repository

import com.kcalfit.app.data.local.ExerciseDao
import com.kcalfit.app.data.local.FoodEntryDao
import com.kcalfit.app.data.local.FoodItemDao
import com.kcalfit.app.data.local.LifestyleDao
import com.kcalfit.app.data.local.UserDao
import com.kcalfit.app.data.model.ActivityPostEntity
import com.kcalfit.app.data.model.CoachMessageEntity
import com.kcalfit.app.data.model.ExerciseEntity
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.data.model.FastingSessionEntity
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.FriendEntity
import com.kcalfit.app.data.model.GroceryItemEntity
import com.kcalfit.app.data.model.MealPlanEntity
import com.kcalfit.app.data.model.RecipeEntity
import com.kcalfit.app.data.model.SavedMealEntity
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.model.WaterEntry
import com.kcalfit.app.data.model.WeightEntry
import com.kcalfit.app.data.model.WorkoutRoutineEntity
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.remote.OpenFoodFactsClient
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodRepository(
    private val foodEntryDao: FoodEntryDao,
    private val userDao: UserDao,
    private val foodItemDao: FoodItemDao,
    private val exerciseDao: ExerciseDao,
    private val lifestyleDao: LifestyleDao,
    private val userPreferences: UserPreferences,
    private val foodApi: OpenFoodFactsClient = OpenFoodFactsClient()
) {

    // --- User Profile ---
    fun getUserProfileFlow(): Flow<UserEntity?> = userDao.getUserProfileFlow()
    suspend fun getUserProfile(): UserEntity? = userDao.getUserProfile()
    suspend fun saveUserProfile(user: UserEntity) = userDao.insertOrUpdateUser(user)
    suspend fun setLoggedIn(isLoggedIn: Boolean) = userDao.setLoggedIn(isLoggedIn)
    suspend fun setOnboarded(isOnboarded: Boolean) = userDao.setOnboarded(isOnboarded)
    suspend fun setEmailVerified(isVerified: Boolean) = userDao.setEmailVerified(isVerified)
    suspend fun setVerificationCode(code: String) = userDao.setVerificationCode(code, System.currentTimeMillis())
    suspend fun getVerificationCode(): String? = userDao.getVerificationCode()

    // --- Food Entries ---
    fun getEntriesForDate(date: String): Flow<List<FoodEntry>> = foodEntryDao.getEntriesForDate(date)
    fun getAllEntries(): Flow<List<FoodEntry>> = foodEntryDao.getAllEntries()
    fun getTotalCaloriesForDate(date: String): Flow<Int?> = foodEntryDao.getTotalCaloriesForDate(date)
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long {
        com.kcalfit.app.domain.calculator.FoodDiaryRules.validate(foodEntry)
        return foodEntryDao.insertFoodEntry(foodEntry.copy(id = 0))
    }
    suspend fun deleteFoodEntry(foodEntry: FoodEntry) = foodEntryDao.deleteFoodEntry(foodEntry)

    // --- Master Foods Search & Barcode ---
    fun searchMasterFoods(query: String): Flow<List<FoodItemEntity>> = foodItemDao.searchMasterFoods(query)
    fun searchCachedApiFoods(query: String): Flow<List<FoodItemEntity>> = foodItemDao.searchCachedApiFoods(query)
    fun searchLiveFoods(query: String): Flow<List<FoodItemEntity>> = flow {
        if (query.trim().length < 2) {
            emit(emptyList())
            return@flow
        }
        val live = foodApi.search(query)
        if (live.isNotEmpty()) {
            foodItemDao.insertFoodItems(live)
            emit(live)
        } else {
            emit(foodItemDao.searchCachedApiFoods(query).first())
        }
    }
    suspend fun findFoodByBarcode(barcode: String): FoodItemEntity? =
        foodApi.lookupBarcode(barcode) ?: foodItemDao.findByBarcode(barcode)
    suspend fun insertCustomFood(item: FoodItemEntity): Long = foodItemDao.insertFoodItem(item)

    // --- Exercise ---
    fun getExerciseEntriesForDate(date: String): Flow<List<ExerciseEntry>> = exerciseDao.getExerciseEntriesForDate(date)
    fun getAllExerciseEntries(): Flow<List<ExerciseEntry>> = exerciseDao.getAllExerciseEntries()
    fun getTotalCaloriesBurnedForDate(date: String): Flow<Int?> = exerciseDao.getTotalCaloriesBurnedForDate(date)
    suspend fun insertExerciseEntry(entry: ExerciseEntry): Long = exerciseDao.insertExerciseEntry(entry)
    suspend fun deleteExerciseEntry(entry: ExerciseEntry) = exerciseDao.deleteExerciseEntry(entry)
    fun getWorkoutRoutines(): Flow<List<WorkoutRoutineEntity>> = exerciseDao.getWorkoutRoutines()
    suspend fun insertWorkoutRoutine(routine: WorkoutRoutineEntity) = exerciseDao.insertWorkoutRoutine(routine)

    // --- Lifestyle & Tracking ---
    fun getTotalWaterForDate(date: String): Flow<Int?> = lifestyleDao.getTotalWaterForDate(date)
    suspend fun addWater(amountMl: Int, date: String) = lifestyleDao.insertWaterEntry(WaterEntry(amountMl = amountMl, date = date))

    fun getLatestWeight(): Flow<WeightEntry?> = lifestyleDao.getLatestWeightEntry()
    fun getAllWeightEntries(): Flow<List<WeightEntry>> = lifestyleDao.getAllWeightEntries()
    suspend fun addWeight(weightKg: Double, date: String, notes: String = "") = lifestyleDao.insertWeightEntry(WeightEntry(weightKg = weightKg, date = date, notes = notes))

    fun getActiveFastingSession(): Flow<FastingSessionEntity?> = lifestyleDao.getActiveFastingSession()
    suspend fun startFasting(protocol: String, durationHours: Int) = lifestyleDao.insertFastingSession(FastingSessionEntity(protocol = protocol, startTimeMillis = System.currentTimeMillis(), targetDurationHours = durationHours))

    fun getAllRecipes(): Flow<List<RecipeEntity>> = lifestyleDao.getAllRecipes()
    suspend fun insertRecipe(recipe: RecipeEntity) = lifestyleDao.insertRecipe(recipe)

    fun getAllSavedMeals(): Flow<List<SavedMealEntity>> = lifestyleDao.getAllSavedMeals()
    suspend fun insertSavedMeal(meal: SavedMealEntity) = lifestyleDao.insertSavedMeal(meal)

    fun getMealPlanForDay(dayOfWeek: String): Flow<List<MealPlanEntity>> = lifestyleDao.getMealPlanForDay(dayOfWeek)
    suspend fun insertMealPlan(entry: MealPlanEntity) = lifestyleDao.insertMealPlanEntry(entry)

    fun getGroceryItems(): Flow<List<GroceryItemEntity>> = lifestyleDao.getGroceryItems()
    suspend fun addGroceryItem(item: GroceryItemEntity) = lifestyleDao.insertGroceryItem(item)
    suspend fun toggleGroceryItem(id: Long, isChecked: Boolean) = lifestyleDao.toggleGroceryItem(id, isChecked)

    fun getCoachMessages(): Flow<List<CoachMessageEntity>> = lifestyleDao.getCoachMessages()
    suspend fun sendCoachMessage(text: String, sender: String) = lifestyleDao.insertCoachMessage(CoachMessageEntity(sender = sender, messageText = text))

    fun getFriendsList(): Flow<List<FriendEntity>> = lifestyleDao.getFriendsList()
    suspend fun addFriend(name: String, email: String) = lifestyleDao.insertFriend(FriendEntity(friendName = name, friendEmail = email))

    fun getActivityPosts(): Flow<List<ActivityPostEntity>> = lifestyleDao.getActivityPosts()
    suspend fun createPost(author: String, text: String, type: String) = lifestyleDao.insertActivityPost(ActivityPostEntity(authorName = author, postText = text, postType = type))

    fun getCalorieGoal(): Int = userPreferences.getCalorieGoal()
    fun setCalorieGoal(goal: Int) = userPreferences.setCalorieGoal(goal)

    // --- Seed Initial Master Food & Demo Data ---
    suspend fun seedSampleDataIfNeeded() {
        // The old preference can outlive a Room destructive migration. Room's actual
        // contents, not a preference flag, determine whether starter foods are needed.
        // Seed Master Exercises dataset
        if (exerciseDao.getMasterExerciseCount() == 0) {
            val masterExercises = listOf(
                ExerciseEntity(name = "Brisk Walking", category = "CARDIO", caloriesBurnedPerMinute = 5.0),
                ExerciseEntity(name = "Running / Jogging", category = "CARDIO", caloriesBurnedPerMinute = 11.5),
                ExerciseEntity(name = "Cycling", category = "CARDIO", caloriesBurnedPerMinute = 8.5),
                ExerciseEntity(name = "Swimming", category = "CARDIO", caloriesBurnedPerMinute = 9.0),
                ExerciseEntity(name = "Weight Training", category = "STRENGTH", caloriesBurnedPerMinute = 6.0),
                ExerciseEntity(name = "Push-ups & Bodyweight", category = "STRENGTH", caloriesBurnedPerMinute = 7.0),
                ExerciseEntity(name = "Yoga & Stretching", category = "CARDIO", caloriesBurnedPerMinute = 4.0)
            )
            exerciseDao.insertMasterExercises(masterExercises)
        }

        // User and tracking data are intentionally never auto-created. Registration owns that flow.

        userPreferences.setSampleSeeded(true)
    }

    suspend fun clearAllData() {
        foodEntryDao.deleteAllEntries()
        userPreferences.setSampleSeeded(false)
    }
}
