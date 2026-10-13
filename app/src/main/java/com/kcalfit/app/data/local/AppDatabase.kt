package com.kcalfit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kcalfit.app.data.model.ActivityPostEntity
import com.kcalfit.app.data.model.CoachMessageEntity
import com.kcalfit.app.data.model.ExerciseEntity
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.data.model.FastingSessionEntity
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.FriendEntity
import com.kcalfit.app.data.model.GroceryItemEntity
import com.kcalfit.app.data.model.MealPlanEntity
import com.kcalfit.app.data.model.RecipeEntity
import com.kcalfit.app.data.model.SavedMealEntity
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.model.WaterEntry
import com.kcalfit.app.data.model.WeightEntry
import com.kcalfit.app.data.model.WorkoutExerciseEntity
import com.kcalfit.app.data.model.WorkoutRoutineEntity

@Database(
    entities = [
        UserEntity::class,
        FoodEntry::class,
        FoodItemEntity::class,
        ExerciseEntity::class,
        ExerciseEntry::class,
        WorkoutRoutineEntity::class,
        WorkoutExerciseEntity::class,
        WaterEntry::class,
        WeightEntry::class,
        FastingSessionEntity::class,
        RecipeEntity::class,
        SavedMealEntity::class,
        MealPlanEntity::class,
        GroceryItemEntity::class,
        CoachMessageEntity::class,
        FriendEntity::class,
        ActivityPostEntity::class
    ],
    // V5 intentionally removes pre-provider demo accounts and local hash credentials.
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun userDao(): UserDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun lifestyleDao(): LifestyleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cal_fit_database"
                )
                    .addMigrations(object : Migration(5, 6) {
                        override fun migrate(db: SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE food_entries ADD COLUMN nutritionKnown INTEGER NOT NULL DEFAULT 1")
                            db.execSQL("UPDATE food_entries SET nutritionKnown = 0 WHERE proteinGrams = 0 AND carbsGrams = 0 AND fatGrams = 0")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
