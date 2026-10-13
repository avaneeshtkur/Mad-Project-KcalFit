package com.kcalfit.app.data.model

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snacks");

    companion object {
        fun fromString(value: String): MealType {
            if (value.equals("Snack", true)) return SNACK
            return entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: SNACK
        }
    }
}
