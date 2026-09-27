package com.kcalfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // CARDIO / STRENGTH
    val caloriesBurnedPerMinute: Double = 8.0,
    val isCustom: Boolean = false
)

@Entity(tableName = "exercise_entries")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String,
    val category: String, // CARDIO / STRENGTH
    val durationMinutes: Int = 30,
    val distanceKm: Double = 0.0,
    val caloriesBurned: Int = 200,
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Double = 0.0,
    val date: String, // Format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineName: String,
    val description: String = ""
)

@Entity(tableName = "workout_exercises")
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val exerciseName: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Double
)
