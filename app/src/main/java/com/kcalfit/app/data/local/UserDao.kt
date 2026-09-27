package com.kcalfit.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kcalfit.app.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserEntity?

    @Query("UPDATE user_profile SET isLoggedIn = :isLoggedIn WHERE id = 1")
    suspend fun setLoggedIn(isLoggedIn: Boolean)

    @Query("UPDATE user_profile SET isOnboarded = :isOnboarded WHERE id = 1")
    suspend fun setOnboarded(isOnboarded: Boolean)

    @Query("UPDATE user_profile SET dailyCalorieGoal = :goal WHERE id = 1")
    suspend fun updateCalorieGoal(goal: Int)
}
