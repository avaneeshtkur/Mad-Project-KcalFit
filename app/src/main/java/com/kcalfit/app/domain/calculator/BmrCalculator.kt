package com.kcalfit.app.domain.calculator

import kotlin.math.roundToInt

enum class Sex {
    MALE, FEMALE
}

enum class ActivityLevel(val displayName: String, val multiplier: Double) {
    SEDENTARY("Sedentary (little or no exercise)", 1.2),
    LIGHTLY_ACTIVE("Lightly Active (1-3 days/week)", 1.375),
    MODERATELY_ACTIVE("Moderately Active (3-5 days/week)", 1.55),
    VERY_ACTIVE("Very Active (6-7 days/week)", 1.725),
    EXTRA_ACTIVE("Extra Active (hard physical job)", 1.9)
}

enum class FitnessGoal(val displayName: String) {
    LOSE_WEIGHT("Lose Weight"),
    MAINTAIN("Maintain Weight"),
    GAIN_WEIGHT("Gain Weight")
}

data class CalorieCalculationResult(
    val bmr: Int,
    val tdee: Int,
    val dailyCalorieTarget: Int,
    val weeklyCalorieAdjustment: Int
)

object BmrCalculator {

    /**
     * Calculates BMR using the Mifflin-St Jeor Equation:
     * For Men: BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age(years) + 5
     * For Women: BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age(years) - 161
     */
    fun calculateBmr(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: Sex
    ): Int {
        val base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
        val bmr = if (sex == Sex.MALE) base + 5 else base - 161
        return bmr.roundToInt().coerceAtLeast(800)
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE) = BMR * Activity Multiplier
     */
    fun calculateTdee(bmr: Int, activityLevel: ActivityLevel): Int {
        return (bmr * activityLevel.multiplier).roundToInt()
    }

    /**
     * Calculates Daily Calorie Target based on goal and weekly change rate (e.g. 0.25kg, 0.5kg, 0.75kg, 1.0kg/week).
     * 0.5 kg weight loss/gain per week ~ 500 kcal daily deficit/surplus.
     */
    fun calculateDailyTarget(
        tdee: Int,
        goal: FitnessGoal,
        weeklyChangeKg: Double = 0.5
    ): CalorieCalculationResult {
        val dailyAdjustment = (weeklyChangeKg * 1000).roundToInt().coerceIn(250, 1000)

        val target = when (goal) {
            FitnessGoal.LOSE_WEIGHT -> (tdee - dailyAdjustment).coerceAtLeast(1200)
            FitnessGoal.MAINTAIN -> tdee
            FitnessGoal.GAIN_WEIGHT -> tdee + dailyAdjustment
        }

        return CalorieCalculationResult(
            bmr = 0, // Set during full calculation
            tdee = tdee,
            dailyCalorieTarget = target,
            weeklyCalorieAdjustment = if (goal == FitnessGoal.LOSE_WEIGHT) -dailyAdjustment else if (goal == FitnessGoal.GAIN_WEIGHT) dailyAdjustment else 0
        )
    }

    fun computeFullProfile(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: Sex,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        weeklyChangeKg: Double = 0.5
    ): CalorieCalculationResult {
        val bmr = calculateBmr(weightKg, heightCm, age, sex)
        val tdee = calculateTdee(bmr, activityLevel)
        val result = calculateDailyTarget(tdee, goal, weeklyChangeKg)
        return result.copy(bmr = bmr)
    }
}
