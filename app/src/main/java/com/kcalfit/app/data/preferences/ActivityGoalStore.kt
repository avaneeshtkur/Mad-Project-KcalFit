package com.kcalfit.app.data.preferences

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ActivityGoal(
    val title: String,
    val activity: String,
    val repeat: String,
    val days: Set<Int>,
    val everyDay: Boolean,
    val reminder: String
)

/** Small account-scoped local store for the goals shown in the KcalFit design. */
class ActivityGoalStore(context: Context) {
    private val prefs = context.getSharedPreferences("nakafit_activity_goals", Context.MODE_PRIVATE)

    fun get(userId: String): List<ActivityGoal> = runCatching {
        val json = JSONArray(prefs.getString(key(userId), "[]"))
        (0 until json.length()).map { index ->
            val item = json.getJSONObject(index)
            val selectedDays = item.optJSONArray("days") ?: JSONArray()
            ActivityGoal(
                title = item.getString("title"),
                activity = item.getString("activity"),
                repeat = item.getString("repeat"),
                days = (0 until selectedDays.length()).map { selectedDays.getInt(it) }.toSet(),
                everyDay = item.optBoolean("everyDay"),
                reminder = item.optString("reminder")
            )
        }
    }.getOrDefault(emptyList())

    fun add(userId: String, goal: ActivityGoal) {
        val existing = JSONArray(prefs.getString(key(userId), "[]"))
        val days = JSONArray().apply { goal.days.sorted().forEach { put(it) } }
        existing.put(JSONObject().put("title", goal.title).put("activity", goal.activity)
            .put("repeat", goal.repeat).put("days", days)
            .put("everyDay", goal.everyDay).put("reminder", goal.reminder))
        prefs.edit().putString(key(userId), existing.toString()).apply()
    }

    private fun key(userId: String) = "goals_${userId.ifBlank { "anonymous" }}"
}
