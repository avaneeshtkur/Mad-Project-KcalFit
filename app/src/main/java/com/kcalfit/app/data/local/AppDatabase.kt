package com.kcalfit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 3,
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

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN firebaseUid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutFrequency TEXT NOT NULL DEFAULT '3'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutDuration TEXT NOT NULL DEFAULT '45'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutLocation TEXT NOT NULL DEFAULT 'GYM'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN fitnessExperience TEXT NOT NULL DEFAULT 'BEGINNER'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutPlanTitle TEXT NOT NULL DEFAULT '3-Day Full Body Foundation'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutPlanDescription TEXT NOT NULL DEFAULT 'A balanced foundational routine for consistency and strength.'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cal_fit_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
