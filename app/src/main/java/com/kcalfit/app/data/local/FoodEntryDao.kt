package com.kcalfit.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kcalfit.app.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntries(foodEntries: List<FoodEntry>)

    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteFoodEntryById(id: Long)

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: String): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries ORDER BY date DESC, timestamp DESC")
    fun getAllEntries(): Flow<List<FoodEntry>>

    @Query("SELECT SUM(calories) FROM food_entries WHERE date = :date")
    fun getTotalCaloriesForDate(date: String): Flow<Int?>

    @Query("SELECT COUNT(*) FROM food_entries")
    suspend fun getEntryCount(): Int

    @Query("DELETE FROM food_entries")
    suspend fun deleteAllEntries()
}
