package com.kcalfit.app.data.repository

import com.kcalfit.app.data.local.FoodEntryDao
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodRepository(
    private val foodEntryDao: FoodEntryDao,
    private val userPreferences: UserPreferences
) {

    fun getEntriesForDate(date: String): Flow<List<FoodEntry>> {
        return foodEntryDao.getEntriesForDate(date)
    }

    fun getAllEntries(): Flow<List<FoodEntry>> {
        return foodEntryDao.getAllEntries()
    }

    fun getTotalCaloriesForDate(date: String): Flow<Int?> {
        return foodEntryDao.getTotalCaloriesForDate(date)
    }

    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long {
        return foodEntryDao.insertFoodEntry(foodEntry)
    }

    suspend fun deleteFoodEntry(foodEntry: FoodEntry) {
        foodEntryDao.deleteFoodEntry(foodEntry)
    }

    suspend fun deleteFoodEntryById(id: Long) {
        foodEntryDao.deleteFoodEntryById(id)
    }

    fun getCalorieGoal(): Int {
        return userPreferences.getCalorieGoal()
    }

    fun setCalorieGoal(goal: Int) {
        userPreferences.setCalorieGoal(goal)
    }

    suspend fun seedSampleDataIfNeeded(force: Boolean = false) {
        if (!force && userPreferences.isSampleSeeded()) return
        if (!force && foodEntryDao.getEntryCount() > 0) return

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val todayDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dayBeforeYesterdayDate = dateFormat.format(calendar.time)

        val sampleEntries = listOf(
            // Today's entries
            FoodEntry(
                foodName = "Oatmeal with Honey & Berries",
                mealType = "Breakfast",
                calories = 320,
                quantity = "1 bowl (250g)",
                date = todayDate,
                timestamp = System.currentTimeMillis() - 14400000
            ),
            FoodEntry(
                foodName = "Boiled Eggs",
                mealType = "Breakfast",
                calories = 155,
                quantity = "2 large eggs",
                date = todayDate,
                timestamp = System.currentTimeMillis() - 12600000
            ),
            FoodEntry(
                foodName = "Grilled Chicken Rice Bowl",
                mealType = "Lunch",
                calories = 580,
                quantity = "1 serving",
                date = todayDate,
                timestamp = System.currentTimeMillis() - 7200000
            ),
            FoodEntry(
                foodName = "Dal Tadka & Roti",
                mealType = "Dinner",
                calories = 420,
                quantity = "1 bowl + 2 rotis",
                date = todayDate,
                timestamp = System.currentTimeMillis() - 1800000
            ),
            FoodEntry(
                foodName = "Apple",
                mealType = "Snack",
                calories = 95,
                quantity = "1 medium",
                date = todayDate,
                timestamp = System.currentTimeMillis() - 3600000
            ),

            // Yesterday's entries
            FoodEntry(
                foodName = "Banana Peanut Butter Smoothie",
                mealType = "Breakfast",
                calories = 380,
                quantity = "1 glass (300ml)",
                date = yesterdayDate,
                timestamp = System.currentTimeMillis() - 86400000 - 14400000
            ),
            FoodEntry(
                foodName = "Vegetable Sandwich",
                mealType = "Lunch",
                calories = 340,
                quantity = "2 slices",
                date = yesterdayDate,
                timestamp = System.currentTimeMillis() - 86400000 - 7200000
            ),
            FoodEntry(
                foodName = "Paneer Curry & Rice",
                mealType = "Dinner",
                calories = 620,
                quantity = "1 serving",
                date = yesterdayDate,
                timestamp = System.currentTimeMillis() - 86400000 - 1800000
            ),

            // Day before yesterday's entries
            FoodEntry(
                foodName = "Greek Yogurt & Granola",
                mealType = "Breakfast",
                calories = 280,
                quantity = "1 cup",
                date = dayBeforeYesterdayDate,
                timestamp = System.currentTimeMillis() - (86400000 * 2) - 14400000
            ),
            FoodEntry(
                foodName = "Chicken Salad",
                mealType = "Lunch",
                calories = 410,
                quantity = "1 bowl",
                date = dayBeforeYesterdayDate,
                timestamp = System.currentTimeMillis() - (86400000 * 2) - 7200000
            ),
            FoodEntry(
                foodName = "Warm Almond Milk",
                mealType = "Snack",
                calories = 120,
                quantity = "1 cup (200ml)",
                date = dayBeforeYesterdayDate,
                timestamp = System.currentTimeMillis() - (86400000 * 2) - 3600000
            )
        )

        foodEntryDao.insertFoodEntries(sampleEntries)
        userPreferences.setSampleSeeded(true)
    }

    suspend fun clearAllData() {
        foodEntryDao.deleteAllEntries()
        userPreferences.setSampleSeeded(false)
    }
}
