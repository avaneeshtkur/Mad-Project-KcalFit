package com.kcalfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Fitness User",
    val email: String = "user@calfit.app",
    val passwordHash: String = "",
    val isLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false,
    val age: Int = 25,
    val sex: String = "MALE", // MALE / FEMALE
    val heightCm: Double = 175.0,
    val weightKg: Double = 70.0,
    val targetWeightKg: Double = 65.0,
    val activityLevel: String = "MODERATELY_ACTIVE",
    val fitnessGoal: String = "LOSE_WEIGHT", // LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT
    val weeklyTargetKg: Double = 0.5,
    val units: String = "METRIC", // METRIC, IMPERIAL
    val dietaryPreference: String = "STANDARD", // STANDARD, HIGH_PROTEIN, KETO, VEGAN, VEGETARIAN, MEDITERRANEAN
    val allergies: String = "",
    val dailyCalorieGoal: Int = 2000,
    val targetProteinGrams: Int = 150,
    val targetCarbsGrams: Int = 200,
    val targetFatGrams: Int = 65,
    val targetWaterMl: Int = 2500,
    val currentStreakDays: Int = 1,
    val lastLoggedDate: String = "",
    // Phase 1 Extended Fields:
    val firebaseUid: String = "",
    val workoutFrequency: String = "3",
    val workoutDuration: String = "45",
    val workoutLocation: String = "GYM",
    val fitnessExperience: String = "BEGINNER",
    val workoutPlanTitle: String = "3-Day Full Body Foundation",
    val workoutPlanDescription: String = "A balanced foundational routine for consistency and strength."
)
