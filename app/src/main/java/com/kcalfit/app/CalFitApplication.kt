package com.kcalfit.app

import android.app.Application
import com.kcalfit.app.data.local.AppDatabase
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.repository.AuthRepository
import com.kcalfit.app.data.repository.FoodRepository
import com.kcalfit.app.data.repository.LocalAuthRepository

class CalFitApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }
    val authRepository: AuthRepository by lazy {
        LocalAuthRepository(
            userDao = database.userDao(),
            userPreferences = userPreferences
        )
    }
    val repository: FoodRepository by lazy {
        FoodRepository(
            foodEntryDao = database.foodEntryDao(),
            userDao = database.userDao(),
            foodItemDao = database.foodItemDao(),
            exerciseDao = database.exerciseDao(),
            lifestyleDao = database.lifestyleDao(),
            userPreferences = userPreferences
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CalFitApplication
            private set
    }
}
