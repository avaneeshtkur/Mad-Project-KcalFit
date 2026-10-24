package com.kcalfit.app

import android.app.Application
import com.kcalfit.app.data.local.AppDatabase
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.repository.FoodRepository
import com.kcalfit.app.data.importer.FoodAssetImporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
        // A live workout exists only in memory, so a notification surviving from an earlier process is stale.
        com.kcalfit.app.data.workout.WorkoutNotifications.cancel(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            FoodAssetImporter.importIfNeeded(this@CalFitApplication, database.foodItemDao())
            repository.foodSearch.invalidate()
        }
    }

    companion object {
        lateinit var instance: CalFitApplication
            private set
    }
}
