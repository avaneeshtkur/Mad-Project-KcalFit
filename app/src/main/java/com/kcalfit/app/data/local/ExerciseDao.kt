package com.kcalfit.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kcalfit.app.data.model.ExerciseEntity
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.data.model.WorkoutRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseEntry(entry: ExerciseEntry): Long

    @Delete
    suspend fun deleteExerciseEntry(entry: ExerciseEntry)

    @Query("SELECT * FROM exercise_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getExerciseEntriesForDate(date: String): Flow<List<ExerciseEntry>>

    @Query("SELECT SUM(caloriesBurned) FROM exercise_entries WHERE date = :date")
    fun getTotalCaloriesBurnedForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM exercise_entries ORDER BY date DESC, timestamp DESC")
    fun getAllExerciseEntries(): Flow<List<ExerciseEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM master_exercises ORDER BY name ASC")
    fun getMasterExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM master_exercises")
    suspend fun getMasterExerciseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutRoutine(routine: WorkoutRoutineEntity): Long

    @Query("SELECT * FROM workout_routines")
    fun getWorkoutRoutines(): Flow<List<WorkoutRoutineEntity>>
}
