package com.kcalfit.app.domain.calculator

import com.kcalfit.app.data.model.MealType

data class ParsedVoiceFoodItem(
    val foodName: String,
    val estimatedQuantity: String,
    val estimatedCalories: Int,
    val estimatedProtein: Int,
    val estimatedCarbs: Int,
    val estimatedFat: Int,
    val detectedMealType: MealType
)

object NlpFoodParser {

    private val foodDatabaseMap = mapOf(
        "egg" to Triple(78, 6, 1),      // calories, protein, carbs, fat=5
        "eggs" to Triple(78, 6, 1),
        "roti" to Triple(120, 3, 22),
        "rotis" to Triple(120, 3, 22),
        "chapati" to Triple(120, 3, 22),
        "chapatis" to Triple(120, 3, 22),
        "rice" to Triple(205, 4, 45),
        "dal" to Triple(180, 12, 28),
        "milk" to Triple(150, 8, 12),
        "apple" to Triple(95, 0, 25),
        "apples" to Triple(95, 0, 25),
        "banana" to Triple(105, 1, 27),
        "bananas" to Triple(105, 1, 27),
        "chicken" to Triple(230, 27, 0),
        "oats" to Triple(150, 5, 27),
        "oatmeal" to Triple(200, 6, 34),
        "sandwich" to Triple(280, 10, 35),
        "paneer" to Triple(260, 18, 6),
        "dosa" to Triple(180, 4, 30),
        "idli" to Triple(70, 2, 15),
        "salad" to Triple(120, 3, 10),
        "coffee" to Triple(60, 1, 8),
        "tea" to Triple(50, 1, 7)
    )

    fun parseVoiceInput(spokenText: String): List<ParsedVoiceFoodItem> {
        val lowerText = spokenText.lowercase()

        val detectedMeal = when {
            lowerText.contains("breakfast") -> MealType.BREAKFAST
            lowerText.contains("lunch") -> MealType.LUNCH
            lowerText.contains("dinner") -> MealType.DINNER
            lowerText.contains("snack") -> MealType.SNACK
            else -> MealType.BREAKFAST
        }

        val results = mutableListOf<ParsedVoiceFoodItem>()

        // Split text by common conjunctions
        val clauses = lowerText.split(" and ", ",", " with ", " for breakfast ", " for lunch ", " for dinner ", " for snack ")

        val numberWords = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5, "a" to 1, "an" to 1
        )

        for (clause in clauses) {
            val words = clause.trim().split(" ")
            var quantity = 1

            for (word in words) {
                if (numberWords.containsKey(word)) {
                    quantity = numberWords[word] ?: 1
                }
            }

            for ((foodKey, nutrition) in foodDatabaseMap) {
                if (clause.contains(foodKey)) {
                    val (baseCals, baseProtein, baseCarbs) = nutrition
                    val totalCals = baseCals * quantity
                    val totalProtein = baseProtein * quantity
                    val totalCarbs = baseCarbs * quantity
                    val totalFat = ((totalCals - (totalProtein * 4 + totalCarbs * 4)) / 9).coerceAtLeast(1)

                    val formattedName = foodKey.replaceFirstChar { it.uppercase() }
                    val item = ParsedVoiceFoodItem(
                        foodName = formattedName,
                        estimatedQuantity = "$quantity serving(s)",
                        estimatedCalories = totalCals,
                        estimatedProtein = totalProtein,
                        estimatedCarbs = totalCarbs,
                        estimatedFat = totalFat,
                        detectedMealType = detectedMeal
                    )

                    if (results.none { it.foodName.equals(formattedName, ignoreCase = true) }) {
                        results.add(item)
                    }
                }
            }
        }

        return results
    }
}
