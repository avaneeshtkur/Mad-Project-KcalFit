package com.kcalfit.app

import android.app.Application
import com.kcalfit.app.data.local.AppDatabase
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.repository.FoodRepository

class CalFitApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }
    val repository: FoodRepository by lazy {
        FoodRepository(database.foodEntryDao(), userPreferences)
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
