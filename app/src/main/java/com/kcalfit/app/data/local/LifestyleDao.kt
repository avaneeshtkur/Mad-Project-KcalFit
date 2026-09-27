package com.kcalfit.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kcalfit.app.data.model.ActivityPostEntity
import com.kcalfit.app.data.model.CoachMessageEntity
import com.kcalfit.app.data.model.FastingSessionEntity
import com.kcalfit.app.data.model.FriendEntity
import com.kcalfit.app.data.model.GroceryItemEntity
import com.kcalfit.app.data.model.MealPlanEntity
import com.kcalfit.app.data.model.RecipeEntity
import com.kcalfit.app.data.model.SavedMealEntity
import com.kcalfit.app.data.model.WaterEntry
import com.kcalfit.app.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LifestyleDao {

    // --- Water ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntry)

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE date = :date")
    fun getTotalWaterForDate(date: String): Flow<Int?>

    // --- Weight ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntry)

    @Query("SELECT * FROM weight_entries ORDER BY date DESC, timestamp DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(): Flow<WeightEntry?>

    // --- Fasting ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFastingSession(session: FastingSessionEntity): Long

    @Query("SELECT * FROM fasting_sessions ORDER BY startTimeMillis DESC LIMIT 1")
    fun getActiveFastingSession(): Flow<FastingSessionEntity?>

    // --- Recipes & Saved Meals ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Query("SELECT * FROM recipes ORDER BY recipeName ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMeal(savedMeal: SavedMealEntity): Long

    @Query("SELECT * FROM saved_meals ORDER BY mealName ASC")
    fun getAllSavedMeals(): Flow<List<SavedMealEntity>>

    // --- Meal Plans & Grocery ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanEntry(entry: MealPlanEntity)

    @Query("SELECT * FROM meal_plans WHERE dayOfWeek = :dayOfWeek")
    fun getMealPlanForDay(dayOfWeek: String): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItemEntity)

    @Query("SELECT * FROM grocery_items ORDER BY isChecked ASC, itemName ASC")
    fun getGroceryItems(): Flow<List<GroceryItemEntity>>

    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :id")
    suspend fun toggleGroceryItem(id: Long, isChecked: Boolean)

    // --- Coach Messages ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoachMessage(message: CoachMessageEntity)

    @Query("SELECT * FROM coach_messages ORDER BY timestamp ASC")
    fun getCoachMessages(): Flow<List<CoachMessageEntity>>

    // --- Social ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("SELECT * FROM friends WHERE status = 'ACCEPTED'")
    fun getFriendsList(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityPost(post: ActivityPostEntity)

    @Query("SELECT * FROM activity_posts ORDER BY timestamp DESC")
    fun getActivityPosts(): Flow<List<ActivityPostEntity>>
}
