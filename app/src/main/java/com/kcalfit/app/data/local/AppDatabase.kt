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
import com.kcalfit.app.data.model.FoodSearchCacheEntity
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
        FoodSearchCacheEntity::class,
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
    // V7 adds bounded food-search cache metadata without touching diary entries.
    version = 7,
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
                            db.execSQL("ALTER TABLE master_foods ADD COLUMN nutritionKnown INTEGER NOT NULL DEFAULT 1")
                            db.execSQL("UPDATE master_foods SET nutritionKnown = 0 WHERE proteinGrams = 0 AND carbsGrams = 0 AND fatGrams = 0")
                            db.execSQL("UPDATE master_foods SET barcode = NULL WHERE barcode IN ('890100100001','890100100002','890100100003','890100100004','890100100005','890100100006','890100100007','890100100008','890100100009','890100100010','890100100011','890100100012','890100100013') AND isCustom = 0 AND brand != 'Imported dataset'")
                        }
                    })
                    .addMigrations(object : Migration(6, 7) {
                        override fun migrate(db: SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE master_foods ADD COLUMN source TEXT NOT NULL DEFAULT 'local'")
                            db.execSQL("ALTER TABLE master_foods ADD COLUMN cachedAtMillis INTEGER NOT NULL DEFAULT 0")
                            db.execSQL("UPDATE master_foods SET source = 'open_food_facts', cachedAtMillis = CAST(strftime('%s','now') AS INTEGER) * 1000 WHERE barcode IS NOT NULL AND isCustom = 0 AND brand != 'Imported dataset'")
                            db.execSQL("DELETE FROM master_foods WHERE source = 'open_food_facts' AND isFavorite = 0 AND id NOT IN (SELECT MIN(id) FROM master_foods WHERE source = 'open_food_facts' GROUP BY barcode)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS food_search_cache (query TEXT NOT NULL, fetchedAtMillis INTEGER NOT NULL, PRIMARY KEY(query))")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
