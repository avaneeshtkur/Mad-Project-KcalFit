package com.kcalfit.app.domain.calculator

import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

data class FoodTotals(val calories: Int, val protein: Double, val carbs: Double, val fat: Double, val missingMacros: Int)
object FoodDiaryRules {
    fun totals(entries: List<FoodEntry>) = FoodTotals(entries.sumOf { it.calories },
        entries.sumOf { it.proteinGrams }, entries.sumOf { it.carbsGrams }, entries.sumOf { it.fatGrams },
        entries.count { !it.nutritionKnown })
    fun validate(entry: FoodEntry) {
        require(entry.foodName.isNotBlank()) { "Enter a food name" }
        require(entry.quantity.isNotBlank()) { "Enter a quantity and unit" }
        require(entry.calories >= 0) { "Calories cannot be negative" }
        LocalDate.parse(entry.date)
        require(listOf(entry.proteinGrams, entry.carbsGrams, entry.fatGrams, entry.fiberGrams,
            entry.sugarGrams, entry.sodiumMg).all { it.isFinite() && it >= 0 }) { "Enter valid non-negative nutrition values" }
    }
    fun multiplier(amount: Double, baseAmount: Double): Double {
        require(amount.isFinite() && amount > 0 && baseAmount.isFinite() && baseAmount > 0) { "Enter a positive quantity" }
        return (amount / baseAmount).also { require(it <= 1000) { "Quantity is too large" } }
    }
    fun calories(base: Int, multiplier: Double): Int {
        val total = base * multiplier
        require(total.isFinite() && total in 0.0..Int.MAX_VALUE.toDouble()) { "Calories are outside the supported range" }
        return total.roundToInt()
    }
    fun entriesFor(entries: List<FoodEntry>, date: String, meal: MealType) =
        entries.filter { it.date == date && MealType.fromString(it.mealType) == meal }
    fun week(date: LocalDate): List<LocalDate> {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0L..6L).map { monday.plusDays(it) }
    }
}
