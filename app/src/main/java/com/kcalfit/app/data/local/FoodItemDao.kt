package com.kcalfit.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.FoodSearchCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT COUNT(*) FROM master_foods")
    fun observeCatalogChanges(): Flow<Int>

    @Query("SELECT * FROM master_foods ORDER BY CASE WHEN source = 'local' OR isCustom = 1 OR isFavorite = 1 THEN 0 ELSE 1 END, cachedAtMillis DESC, id ASC LIMIT 10000")
    suspend fun searchCatalog(): List<FoodItemEntity>

    @Query("SELECT fetchedAtMillis FROM food_search_cache WHERE query = :query")
    suspend fun searchRefreshedAt(query: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSearchKey(key: FoodSearchCacheEntity)

    @Query("SELECT * FROM master_foods WHERE source = 'open_food_facts' AND isCustom = 0 AND barcode = :barcode LIMIT 1")
    suspend fun cachedProduct(barcode: String): FoodItemEntity?

    @Query("DELETE FROM master_foods WHERE source = 'open_food_facts' AND isCustom = 0 AND isFavorite = 0 AND (cachedAtMillis < :oldest OR id NOT IN (SELECT id FROM master_foods WHERE source = 'open_food_facts' AND isCustom = 0 AND isFavorite = 0 ORDER BY cachedAtMillis DESC, id ASC LIMIT :maxFoods))")
    suspend fun pruneRemoteFoods(oldest: Long, maxFoods: Int): Int

    @Query("DELETE FROM food_search_cache")
    suspend fun clearSearchKeys()

    @Query("DELETE FROM food_search_cache WHERE fetchedAtMillis < :oldest OR query NOT IN (SELECT query FROM food_search_cache ORDER BY fetchedAtMillis DESC, query ASC LIMIT :maxKeys)")
    suspend fun pruneSearchKeys(oldest: Long, maxKeys: Int)

    @Transaction
    suspend fun cacheSearch(query: String, foods: List<FoodItemEntity>, now: Long, oldest: Long, maxFoods: Int, maxKeys: Int) {
        foods.distinctBy { it.barcode }.forEach { food ->
            val barcode = food.barcode?.takeIf { it.isNotBlank() } ?: return@forEach
            val previous = cachedProduct(barcode)
            insertFoodItem(food.copy(id = previous?.id ?: 0, isFavorite = previous?.isFavorite ?: false,
                source = "open_food_facts", cachedAtMillis = now))
        }
        if (pruneRemoteFoods(oldest, maxFoods) > 0) clearSearchKeys()
        saveSearchKey(FoodSearchCacheEntity(query, now))
        pruneSearchKeys(oldest, maxKeys)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)

    @Query("SELECT * FROM master_foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMasterFoods(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM master_foods WHERE barcode IS NOT NULL AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchCachedApiFoods(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM master_foods WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): FoodItemEntity?

    @Query("SELECT * FROM master_foods WHERE name = :name AND brand = :brand LIMIT 1")
    suspend fun findByNameAndBrand(name: String, brand: String): FoodItemEntity?

    @Query("SELECT * FROM master_foods WHERE isFavorite = 1")
    fun getFavoriteFoods(): Flow<List<FoodItemEntity>>

    @Query("SELECT COUNT(*) FROM master_foods")
    suspend fun getMasterFoodCount(): Int

    @Query("SELECT COUNT(*) FROM master_foods WHERE brand = 'Imported dataset'")
    suspend fun getImportedFoodCount(): Int
}
