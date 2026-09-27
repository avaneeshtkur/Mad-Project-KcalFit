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
        private const val KEY_IS_ONBOARDING_COMPLETED = "key_is_onboarding_completed"
        private const val KEY_LOGGED_IN_EMAIL = "key_logged_in_email"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_DISPLAY_NAME = "key_display_name"
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

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_IS_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDING_COMPLETED, completed).apply()
    }

    fun getLoggedInEmail(): String? {
        return prefs.getString(KEY_LOGGED_IN_EMAIL, null)
    }

    fun setLoggedInEmail(email: String?) {
        prefs.edit().putString(KEY_LOGGED_IN_EMAIL, email).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun setUserId(userId: String?) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getDisplayName(): String {
        return prefs.getString(KEY_DISPLAY_NAME, "Fitness User") ?: "Fitness User"
    }

    fun setDisplayName(displayName: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, displayName).apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_IS_ONBOARDING_COMPLETED, false)
            .remove(KEY_LOGGED_IN_EMAIL)
            .remove(KEY_USER_ID)
            .remove(KEY_DISPLAY_NAME)
            .apply()
    }
}
