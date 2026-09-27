package com.kcalfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_foods")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String = "Generic",
    val category: String = "General",
    val servingSize: Double = 1.0,
    val servingUnit: String = "serving",
    val calories: Int,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val fiberGrams: Double = 0.0,
    val sugarGrams: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val barcode: String? = null,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)
