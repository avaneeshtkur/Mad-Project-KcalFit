package com.kcalfit.app

import android.app.Application
import com.kcalfit.app.data.local.AppDatabase
import com.kcalfit.app.data.importer.FoodAssetImporter
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.repository.FoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalFitApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }
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
        CoroutineScope(Dispatchers.IO).launch {
            FoodAssetImporter.importIfNeeded(this@CalFitApplication, database.foodItemDao())
        }
    }

    companion object {
        lateinit var instance: CalFitApplication
            private set
    }
}
