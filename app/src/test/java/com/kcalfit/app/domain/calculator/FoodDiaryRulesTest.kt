package com.kcalfit.app.domain.calculator

import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class FoodDiaryRulesTest {
    private fun rice(id: Long, date: String = "2026-10-14", meal: String = "Breakfast") = FoodEntry(
        id = id, foodName = "Rice", mealType = meal, calories = 250, proteinGrams = 5.0,
        carbsGrams = 55.0, fatGrams = .5, quantity = "250 g", date = date)

    @Test fun quantityScalingUsesCatalogueBase() {
        val factor = FoodDiaryRules.multiplier(250.0, 100.0)
        assertEquals(2.5, factor, 0.0)
        assertEquals(325, FoodDiaryRules.calories(130, factor))
        assertEquals(7.5, 3.0 * factor, .0001)
    }
    @Test fun sameNameEntriesRemainDistinctAndDeletingOneRecalculatesTotals() {
        val entries = listOf(rice(1), rice(2), rice(3, meal = "Lunch"), rice(4, date = "2026-10-13"))
        val breakfast = FoodDiaryRules.entriesFor(entries, "2026-10-14", MealType.BREAKFAST)
        assertEquals(500, breakfast.sumOf { it.calories })
        assertEquals(10.0, breakfast.sumOf { it.proteinGrams }, .0)
        val after = entries.filterNot { it.id == 1L }
        assertEquals(listOf(2L), FoodDiaryRules.entriesFor(after, "2026-10-14", MealType.BREAKFAST).map { it.id })
        assertEquals(250, FoodDiaryRules.entriesFor(after, "2026-10-14", MealType.BREAKFAST).sumOf { it.calories })
        assertEquals(1, FoodDiaryRules.entriesFor(after, "2026-10-14", MealType.LUNCH).size)
    }
    @Test fun weekIsUniqueMondayToSundayAcrossMonthAndYear() {
        val week = FoodDiaryRules.week(LocalDate.parse("2027-01-01"))
        assertEquals(DayOfWeek.MONDAY, week.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, week.last().dayOfWeek)
        assertEquals(7, week.toSet().size)
        assertEquals(LocalDate.parse("2026-12-28"), week.first())
    }
    @Test fun historicalSnackAliasStillResolves() {
        assertEquals(MealType.SNACK, MealType.fromString("Snack"))
        assertEquals(MealType.SNACK, MealType.fromString("Snacks"))
    }
    @Test fun explicitZeroNutritionIsValid() { FoodDiaryRules.validate(rice(1).copy(proteinGrams = 0.0, carbsGrams = 0.0, fatGrams = 0.0)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsNegativeNutrition() { FoodDiaryRules.validate(rice(1).copy(carbsGrams = -1.0)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsNonfiniteNutrition() { FoodDiaryRules.validate(rice(1).copy(proteinGrams = Double.NaN)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsZeroQuantity() { FoodDiaryRules.multiplier(0.0, 100.0) }
    @Test(expected = IllegalArgumentException::class) fun rejectsNonfiniteQuantity() { FoodDiaryRules.multiplier(Double.POSITIVE_INFINITY, 100.0) }
}
