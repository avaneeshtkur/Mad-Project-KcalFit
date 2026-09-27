package com.kcalfit.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kcalfit.app.data.model.FoodItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)

    @Query("SELECT * FROM master_foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMasterFoods(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM master_foods WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): FoodItemEntity?

    @Query("SELECT * FROM master_foods WHERE isFavorite = 1")
    fun getFavoriteFoods(): Flow<List<FoodItemEntity>>

    @Query("SELECT COUNT(*) FROM master_foods")
    suspend fun getMasterFoodCount(): Int
}
