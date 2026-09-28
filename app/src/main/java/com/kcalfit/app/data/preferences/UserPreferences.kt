package com.kcalfit.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cal_fit_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CALORIE_GOAL = "key_calorie_goal"
        private const val KEY_IS_SAMPLE_SEEDED = "key_is_sample_seeded"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_IS_ONBOARDED = "key_is_onboarded"
        private const val KEY_USER_EMAIL = "key_user_email"
        const val DEFAULT_CALORIE_GOAL = 2000
    }

    // --- Calorie Goal ---
    fun getCalorieGoal(): Int {
        return prefs.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
    }

    fun setCalorieGoal(goal: Int) {
        prefs.edit().putInt(KEY_CALORIE_GOAL, goal).apply()
    }

    // --- Sample Data Seeded ---
    fun isSampleSeeded(): Boolean {
        return prefs.getBoolean(KEY_IS_SAMPLE_SEEDED, false)
    }

    fun setSampleSeeded(seeded: Boolean) {
        prefs.edit().putBoolean(KEY_IS_SAMPLE_SEEDED, seeded).apply()
    }

    // --- Session State ---
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isOnboarded(): Boolean {
        return prefs.getBoolean(KEY_IS_ONBOARDED, false)
    }

    fun setOnboarded(onboarded: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDED, onboarded).apply()
    }

    // --- User Email ---
    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    // --- Clear Session (for logout) ---
    fun clearSession() {
        prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_IS_ONBOARDED)
            .remove(KEY_USER_EMAIL)
            .apply()
    }
}
