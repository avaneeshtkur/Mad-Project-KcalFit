package com.kcalfit.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cal_fit_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CALORIE_GOAL = "key_calorie_goal"
        private const val KEY_IS_SAMPLE_SEEDED = "key_is_sample_seeded"
        const val DEFAULT_CALORIE_GOAL = 2000
    }

    fun getCalorieGoal(): Int {
        return prefs.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
    }

    fun setCalorieGoal(goal: Int) {
        prefs.edit().putInt(KEY_CALORIE_GOAL, goal).apply()
    }

    fun isSampleSeeded(): Boolean {
        return prefs.getBoolean(KEY_IS_SAMPLE_SEEDED, false)
    }

    fun setSampleSeeded(seeded: Boolean) {
        prefs.edit().putBoolean(KEY_IS_SAMPLE_SEEDED, seeded).apply()
    }
}
