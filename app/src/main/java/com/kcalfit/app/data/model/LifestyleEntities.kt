package com.kcalfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val date: String, // Format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weightKg: Double,
    val bodyFatPercent: Double = 0.0,
    val waistCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val armsCm: Double = 0.0,
    val hipsCm: Double = 0.0,
    val date: String, // Format: YYYY-MM-DD
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fasting_sessions")
data class FastingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val protocol: String = "16:8", // 16:8, 14:10, 12:12, 18:6
    val startTimeMillis: Long,
    val endTimeMillis: Long = 0,
    val targetDurationHours: Int = 16,
    val isCompleted: Boolean = false
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeName: String,
    val instructions: String = "",
    val servings: Int = 1,
    val prepTimeMinutes: Int = 15,
    val category: String = "General",
    val totalCalories: Int = 0,
    val totalProteinGrams: Double = 0.0,
    val totalCarbsGrams: Double = 0.0,
    val totalFatGrams: Double = 0.0,
    val isFavorite: Boolean = false
)

@Entity(tableName = "saved_meals")
data class SavedMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealName: String,
    val defaultMealType: String = "Breakfast",
    val totalCalories: Int = 0
)

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: String, // MONDAY..SUNDAY
    val mealType: String,
    val foodName: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double
)

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val quantityString: String,
    val category: String = "General",
    val isChecked: Boolean = false
)

@Entity(tableName = "coach_messages")
data class CoachMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // USER / COACH
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val friendName: String,
    val friendEmail: String,
    val status: String = "ACCEPTED" // PENDING / ACCEPTED
)

@Entity(tableName = "activity_posts")
data class ActivityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val postText: String,
    val postType: String = "WORKOUT", // WORKOUT / STREAK / MILESTONE
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0
)
