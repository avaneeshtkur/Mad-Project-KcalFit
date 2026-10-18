package com.kcalfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_search_cache")
data class FoodSearchCacheEntity(@PrimaryKey val query: String, val fetchedAtMillis: Long)
