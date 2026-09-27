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
import kotlinx.coroutines.flow.Flow
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
    private val userPreferences: UserPreferences
) {

    // --- User Profile ---
    fun getUserProfileFlow(): Flow<UserEntity?> = userDao.getUserProfileFlow()
    suspend fun getUserProfile(): UserEntity? = userDao.getUserProfile()
    suspend fun saveUserProfile(user: UserEntity) = userDao.insertOrUpdateUser(user)
    suspend fun setLoggedIn(isLoggedIn: Boolean) = userDao.setLoggedIn(isLoggedIn)
    suspend fun setOnboarded(isOnboarded: Boolean) = userDao.setOnboarded(isOnboarded)

    // --- Food Entries ---
    fun getEntriesForDate(date: String): Flow<List<FoodEntry>> = foodEntryDao.getEntriesForDate(date)
    fun getAllEntries(): Flow<List<FoodEntry>> = foodEntryDao.getAllEntries()
    fun getTotalCaloriesForDate(date: String): Flow<Int?> = foodEntryDao.getTotalCaloriesForDate(date)
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long = foodEntryDao.insertFoodEntry(foodEntry)
    suspend fun deleteFoodEntry(foodEntry: FoodEntry) = foodEntryDao.deleteFoodEntry(foodEntry)

    // --- Master Foods Search & Barcode ---
    fun searchMasterFoods(query: String): Flow<List<FoodItemEntity>> = foodItemDao.searchMasterFoods(query)
    suspend fun findFoodByBarcode(barcode: String): FoodItemEntity? = foodItemDao.findByBarcode(barcode)
    suspend fun insertCustomFood(item: FoodItemEntity): Long = foodItemDao.insertFoodItem(item)

    // --- Exercise ---
    fun getExerciseEntriesForDate(date: String): Flow<List<ExerciseEntry>> = exerciseDao.getExerciseEntriesForDate(date)
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
    suspend fun seedSampleDataIfNeeded(force: Boolean = false) {
        if (userPreferences.isSampleSeeded() && !force) return

        // Seed Master Foods dataset
        if (foodItemDao.getMasterFoodCount() == 0 || force) {
            val masterFoods = listOf(
                FoodItemEntity(name = "Roti / Chapati", brand = "Indian Homemade", category = "Grains", calories = 120, proteinGrams = 3.5, carbsGrams = 22.0, fatGrams = 1.2, barcode = "890100100001"),
                FoodItemEntity(name = "Steamed Rice", brand = "Generic", category = "Grains", calories = 205, proteinGrams = 4.2, carbsGrams = 45.0, fatGrams = 0.4, barcode = "890100100002"),
                FoodItemEntity(name = "Dal Tadka", brand = "Indian Home Style", category = "Lentils", calories = 180, proteinGrams = 9.0, carbsGrams = 24.0, fatGrams = 5.0, barcode = "890100100003"),
                FoodItemEntity(name = "Paneer Tikka", brand = "Indian Tandoori", category = "Dairy", calories = 260, proteinGrams = 16.0, carbsGrams = 6.0, fatGrams = 18.0, barcode = "890100100004"),
                FoodItemEntity(name = "Chicken Breast", brand = "Fresh Meat", category = "Meat", calories = 165, proteinGrams = 31.0, carbsGrams = 0.0, fatGrams = 3.6, barcode = "890100100005"),
                FoodItemEntity(name = "Boiled Egg", brand = "Farm Fresh", category = "Eggs", calories = 78, proteinGrams = 6.3, carbsGrams = 0.6, fatGrams = 5.3, barcode = "890100100006"),
                FoodItemEntity(name = "Oatmeal with Milk", brand = "Breakfast Bowl", category = "Grains", calories = 220, proteinGrams = 8.0, carbsGrams = 34.0, fatGrams = 4.5, barcode = "890100100007"),
                FoodItemEntity(name = "Banana", brand = "Fresh Fruit", category = "Fruits", calories = 105, proteinGrams = 1.3, carbsGrams = 27.0, fatGrams = 0.3, barcode = "890100100008"),
                FoodItemEntity(name = "Apple", brand = "Fresh Fruit", category = "Fruits", calories = 95, proteinGrams = 0.5, carbsGrams = 25.0, fatGrams = 0.3, barcode = "890100100009"),
                FoodItemEntity(name = "Masala Dosa", brand = "South Indian", category = "Breakfast", calories = 250, proteinGrams = 5.0, carbsGrams = 38.0, fatGrams = 10.0, barcode = "890100100010"),
                FoodItemEntity(name = "Steamed Idli", brand = "South Indian", category = "Breakfast", calories = 70, proteinGrams = 2.0, carbsGrams = 15.0, fatGrams = 0.2, barcode = "890100100011"),
                FoodItemEntity(name = "Greek Yogurt", brand = "Amul / Nestle", category = "Dairy", calories = 120, proteinGrams = 10.0, carbsGrams = 8.0, fatGrams = 3.0, barcode = "890100100012"),
                FoodItemEntity(name = "Whole Almonds", brand = "Nutraj", category = "Snacks", calories = 160, proteinGrams = 6.0, carbsGrams = 6.0, fatGrams = 14.0, barcode = "890100100013")
            )
            foodItemDao.insertFoodItems(masterFoods)
        }

        // Seed Master Exercises dataset
        if (exerciseDao.getMasterExerciseCount() == 0 || force) {
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

        // Seed Default User Profile if empty
        if (userDao.getUserProfile() == null) {
            userDao.insertOrUpdateUser(
                UserEntity(
                    id = 1,
                    name = "Avaneesh Thakur",
                    email = "avaneesh@calfit.app",
                    isLoggedIn = true,
                    isOnboarded = true,
                    age = 22,
                    sex = "MALE",
                    heightCm = 175.0,
                    weightKg = 72.0,
                    targetWeightKg = 68.0,
                    activityLevel = "MODERATELY_ACTIVE",
                    fitnessGoal = "LOSE_WEIGHT",
                    dailyCalorieGoal = 2100,
                    targetProteinGrams = 150,
                    targetCarbsGrams = 210,
                    targetFatGrams = 60,
                    currentStreakDays = 7,
                    lastLoggedDate = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())
                )
            )
        }

        // Seed Today's Demo Food Entries
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        if (foodEntryDao.getEntryCount() == 0 || force) {
            val sampleEntries = listOf(
                FoodEntry(foodName = "Oatmeal with Honey & Nuts", brand = "Homemade", mealType = "Breakfast", calories = 320, proteinGrams = 9.0, carbsGrams = 48.0, fatGrams = 6.0, quantity = "1 bowl (250g)", date = todayDate),
                FoodEntry(foodName = "Boiled Eggs", brand = "Farm Fresh", mealType = "Breakfast", calories = 155, proteinGrams = 13.0, carbsGrams = 1.0, fatGrams = 10.5, quantity = "2 eggs", date = todayDate),
                FoodEntry(foodName = "Grilled Chicken & Brown Rice", brand = "Fitness Meal", mealType = "Lunch", calories = 520, proteinGrams = 42.0, carbsGrams = 54.0, fatGrams = 8.0, quantity = "1 bowl", date = todayDate),
                FoodEntry(foodName = "Dal Tadka & Roti", brand = "Home Made", mealType = "Dinner", calories = 410, proteinGrams = 16.0, carbsGrams = 62.0, fatGrams = 9.0, quantity = "1 bowl + 2 rotis", date = todayDate),
                FoodEntry(foodName = "Apple & Almonds", brand = "Fresh", mealType = "Snack", calories = 180, proteinGrams = 4.0, carbsGrams = 26.0, fatGrams = 7.0, quantity = "1 apple + 10 almonds", date = todayDate)
            )
            foodEntryDao.insertFoodEntries(sampleEntries)

            // Seed Water & Weight Logs for today
            lifestyleDao.insertWaterEntry(WaterEntry(amountMl = 1750, date = todayDate))
            lifestyleDao.insertWeightEntry(WeightEntry(weightKg = 72.0, date = todayDate, notes = "Morning weight"))
            exerciseDao.insertExerciseEntry(ExerciseEntry(exerciseName = "Morning Jogging", category = "CARDIO", durationMinutes = 30, distanceKm = 4.2, caloriesBurned = 320, date = todayDate))

            // Seed Initial Coach Welcome Message
            lifestyleDao.insertCoachMessage(CoachMessageEntity(sender = "COACH", messageText = "Welcome to Cal Fit! I'm your AI Nutrition Assistant. Ask me anything about your daily meals, macros, or calorie target!"))
        }

        userPreferences.setSampleSeeded(true)
    }

    suspend fun clearAllData() {
        foodEntryDao.deleteAllEntries()
        userPreferences.setSampleSeeded(false)
    }
}
