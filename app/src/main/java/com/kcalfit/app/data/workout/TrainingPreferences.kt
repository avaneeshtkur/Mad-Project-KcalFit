package com.kcalfit.app.data.workout

import android.content.Context
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.workout.Equipment
import com.kcalfit.app.domain.workout.Experience
import com.kcalfit.app.domain.workout.TrainingInputs

/**
 * Session length, level and equipment as picked on the Exercise page. Onboarding never asks for
 * these, so an unset key stays null and the plan labels its default instead of presenting it as
 * the user's choice. Shares the file the weekly training plan already uses.
 */
class TrainingPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("nakafit_training_preferences", Context.MODE_PRIVATE)

    val sessionMinutes: Int? get() = prefs.getInt(MINUTES, 0).takeIf { it > 0 }
    val experience: Experience? get() = prefs.getString(EXPERIENCE, null)?.let { name -> Experience.entries.firstOrNull { it.name == name } }
    val equipment: Equipment? get() = prefs.getString(EQUIPMENT, null)?.let { name -> Equipment.entries.firstOrNull { it.name == name } }

    fun setSessionMinutes(minutes: Int) = prefs.edit().putInt(MINUTES, minutes).apply()
    fun setExperience(experience: Experience) = prefs.edit().putString(EXPERIENCE, experience.name).apply()
    fun setEquipment(equipment: Equipment) = prefs.edit().putString(EQUIPMENT, equipment.name).apply()

    /** Goal and training days come from the profile, and only once onboarding has set them. */
    fun inputs(profile: UserEntity?): TrainingInputs {
        val onboarded = profile?.isOnboarded == true
        return TrainingInputs(
            goal = if (onboarded) FitnessGoal.entries.firstOrNull { it.name == profile?.fitnessGoal } else null,
            daysPerWeek = if (onboarded) profile?.exerciseDaysPerWeek?.takeIf { it in 1..7 } else null,
            experience = experience,
            sessionMinutes = sessionMinutes,
            equipment = equipment
        )
    }

    private companion object {
        const val MINUTES = "today_session_minutes"
        const val EXPERIENCE = "today_experience"
        const val EQUIPMENT = "today_equipment"
    }
}
