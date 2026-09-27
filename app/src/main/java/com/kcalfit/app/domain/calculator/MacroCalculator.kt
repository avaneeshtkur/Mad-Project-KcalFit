package com.kcalfit.app.domain.calculator

import kotlin.math.roundToInt

data class MacroTargets(
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinPercent: Int = 30,
    val carbsPercent: Int = 40,
    val fatPercent: Int = 30
)

data class MealCalorieTargets(
    val breakfastCalories: Int,
    val lunchCalories: Int,
    val dinnerCalories: Int,
    val snackCalories: Int
)

object MacroCalculator {

    /**
     * Calculates Macronutrient Targets in grams from total daily calories.
     * Protein = 4 kcal / gram
     * Carbs = 4 kcal / gram
     * Fat = 9 kcal / gram
     */
    fun calculateMacros(
        dailyCalories: Int,
        proteinPercent: Int = 30,
        carbsPercent: Int = 40,
        fatPercent: Int = 30
    ): MacroTargets {
        val proteinCalories = dailyCalories * (proteinPercent / 100.0)
        val carbsCalories = dailyCalories * (carbsPercent / 100.0)
        val fatCalories = dailyCalories * (fatPercent / 100.0)

        val proteinGrams = (proteinCalories / 4.0).roundToInt()
        val carbsGrams = (carbsCalories / 4.0).roundToInt()
        val fatGrams = (fatCalories / 9.0).roundToInt()

        return MacroTargets(
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            proteinPercent = proteinPercent,
            carbsPercent = carbsPercent,
            fatPercent = fatPercent
        )
    }

    /**
     * Calculates meal calorie distribution (Default: Breakfast 25%, Lunch 35%, Dinner 30%, Snack 10%).
     */
    fun calculateMealTargets(
        dailyCalories: Int,
        breakfastPercent: Int = 25,
        lunchPercent: Int = 35,
        dinnerPercent: Int = 30,
        snackPercent: Int = 10
    ): MealCalorieTargets {
        return MealCalorieTargets(
            breakfastCalories = (dailyCalories * (breakfastPercent / 100.0)).roundToInt(),
            lunchCalories = (dailyCalories * (lunchPercent / 100.0)).roundToInt(),
            dinnerCalories = (dailyCalories * (dinnerPercent / 100.0)).roundToInt(),
            snackCalories = (dailyCalories * (snackPercent / 100.0)).roundToInt()
        )
    }
}
