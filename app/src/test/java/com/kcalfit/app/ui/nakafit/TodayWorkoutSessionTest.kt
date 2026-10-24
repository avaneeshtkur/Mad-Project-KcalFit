package com.kcalfit.app.ui.nakafit

import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.workout.DailyWorkoutGenerator
import com.kcalfit.app.domain.workout.Experience
import com.kcalfit.app.domain.workout.TrainingInputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodayWorkoutSessionTest {
    private val workout = DailyWorkoutGenerator.generate(
        TrainingInputs(FitnessGoal.LOSE_WEIGHT, 3, Experience.BEGINNER, 30, null), LocalDate.of(2026, 10, 19))

    @Test fun startingTodaysPlanUsesTheSharedSessionWithItsPrescriptions() {
        val spec = workout.toNakaWorkout(Experience.BEGINNER).sessionSpec()
        assertEquals(TODAY_WORKOUT_ID, spec.workoutId)
        assertEquals(workout.moves.map { it.exercise.name }, spec.exercises)
        assertEquals(spec.exercises.size, spec.exerciseNotes.size)
        assertTrue(spec.exerciseNotes.first().startsWith("Warm-up · "))
        assertEquals(workout.estimatedMinutes, spec.plannedMinutes)
        assertFalse(spec.tracksSteps)
    }

    @Test fun otherWorkoutsAreUnchanged() {
        val spec = workoutForId("strength").sessionSpec()
        assertTrue(spec.exerciseNotes.isEmpty())
        assertEquals(listOf("Bodyweight squat", "Reverse lunge", "Glute bridge", "Standing calf raise"), spec.exercises)
    }
}
