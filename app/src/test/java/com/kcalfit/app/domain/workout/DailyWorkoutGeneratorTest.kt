package com.kcalfit.app.domain.workout

import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.workout.DailyWorkoutGenerator as G
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DailyWorkoutGeneratorTest {
    private val monday = LocalDate.of(2026, 10, 19)
    private val week = (0L..6L).map { monday.plusDays(it) }
    private val goals = FitnessGoal.entries + listOf<FitnessGoal?>(null)

    private fun plan(goal: FitnessGoal? = FitnessGoal.MAINTAIN, days: Int? = 3, experience: Experience? = Experience.INTERMEDIATE,
                     minutes: Int? = 45, equipment: Equipment? = Equipment.DUMBBELLS, date: LocalDate = monday) =
        G.generate(TrainingInputs(goal, days, experience, minutes, equipment), date)

    @Test fun neverPlansMoreThanTheAvailableTime() {
        for (goal in goals) for (experience in Experience.entries) for (equipment in Equipment.entries)
            for (minutes in G.MINUTE_OPTIONS + listOf(10, 90)) for (days in 1..7) for (date in week) {
                val workout = plan(goal, days, experience, minutes, equipment, date)
                assertTrue("$goal $experience $equipment $minutes min $days days ${date.dayOfWeek}: ${workout.estimatedSeconds}s",
                    workout.estimatedSeconds <= minutes * 60)
            }
    }

    @Test fun everyTrainingDayHasWarmUpWorkAndCoolDown() {
        for (minutes in G.MINUTE_OPTIONS) for (days in 1..7) for (date in week) {
            val workout = plan(minutes = minutes, days = days, date = date)
            if (workout.isRestDay) continue
            assertTrue(workout.phase(WorkoutPhase.WARM_UP).isNotEmpty())
            assertTrue("$minutes min, $days days", workout.phase(WorkoutPhase.MAIN).isNotEmpty())
            assertTrue(workout.phase(WorkoutPhase.COOL_DOWN).isNotEmpty())
        }
    }

    @Test fun shortSessionsGetFewerExercisesThanLongOnes() {
        val short = plan(minutes = 15, experience = Experience.ADVANCED).phase(WorkoutPhase.MAIN).size
        val long = plan(minutes = 60, experience = Experience.ADVANCED).phase(WorkoutPhase.MAIN).size
        assertTrue("$short vs $long", short < long)
    }

    @Test fun onlyUsesAvailableEquipmentAndSuitableExercises() {
        for (equipment in Equipment.entries) for (experience in Experience.entries) for (date in week) {
            plan(equipment = equipment, experience = experience, days = 6, minutes = 60, date = date).moves.forEach {
                assertTrue("${it.exercise.name} needs ${it.exercise.equipment}", it.exercise.equipment <= equipment)
                assertTrue("${it.exercise.name} is not for ${experience.label}", it.exercise.minExperience <= experience)
            }
        }
        val names = plan(equipment = Equipment.BODYWEIGHT, experience = Experience.BEGINNER).moves.map { it.exercise.id }
        assertFalse("bench_press" in names || "goblet_squat" in names)
    }

    @Test fun experienceScalesVolume() {
        val beginner = plan(experience = Experience.BEGINNER, minutes = 60)
        val advanced = plan(experience = Experience.ADVANCED, minutes = 60)
        assertTrue(beginner.phase(WorkoutPhase.MAIN).all { it.sets <= 2 })
        assertTrue(beginner.phase(WorkoutPhase.MAIN).size <= G.maxExercises(Experience.BEGINNER))
        assertTrue(advanced.phase(WorkoutPhase.MAIN).sumOf { it.sets } > beginner.phase(WorkoutPhase.MAIN).sumOf { it.sets })
    }

    @Test fun goalShapesRepsRestAndConditioning() {
        fun squat(goal: FitnessGoal) = plan(goal = goal, equipment = Equipment.DUMBBELLS)
            .phase(WorkoutPhase.MAIN).first { it.exercise.id == "goblet_squat" }
        assertEquals(8..12, squat(FitnessGoal.GAIN_WEIGHT).reps)
        assertEquals(90, squat(FitnessGoal.GAIN_WEIGHT).restSeconds)
        assertEquals(12..15, squat(FitnessGoal.LOSE_WEIGHT).reps)
        assertEquals(45, squat(FitnessGoal.LOSE_WEIGHT).restSeconds)
        assertTrue(plan(goal = FitnessGoal.LOSE_WEIGHT, minutes = 60).phase(WorkoutPhase.FINISHER).isNotEmpty())
        assertTrue(plan(goal = FitnessGoal.MAINTAIN, minutes = 60).phase(WorkoutPhase.FINISHER).isEmpty())
    }

    @Test fun scheduleHasTheRequestedNumberOfTrainingDays() {
        for (days in 1..7) {
            val training = week.count { !plan(days = days, date = it).isRestDay }
            // Seven days a week keeps Sunday as active recovery.
            assertEquals(if (days == 7) 6 else days, training)
        }
    }

    @Test fun sameMusclesAreNotTrainedHardOnConsecutiveDays() {
        fun lower(f: SessionFocus) = f in setOf(SessionFocus.FULL_BODY_A, SessionFocus.FULL_BODY_B, SessionFocus.LOWER)
        fun upper(f: SessionFocus) = f in setOf(SessionFocus.FULL_BODY_A, SessionFocus.FULL_BODY_B, SessionFocus.UPPER)
        for (days in 1..7) {
            val foci = (0L..13L).map { G.focusFor(monday.plusDays(it), days) }
            foci.zipWithNext().forEachIndexed { i, (a, b) ->
                assertFalse("$days days, day $i: $a then $b", (lower(a) && lower(b)) || (upper(a) && upper(b)))
            }
        }
    }

    @Test fun consecutiveFullBodySessionsVary() {
        val mon = plan(days = 3, date = monday).phase(WorkoutPhase.MAIN).map { it.exercise.id }
        val wed = plan(days = 3, date = monday.plusDays(2)).phase(WorkoutPhase.MAIN).map { it.exercise.id }
        assertNotEquals(mon, wed)
    }

    @Test fun restDayOffersOnlyGentleStretching() {
        val sunday = plan(days = 3, date = week.first { it.dayOfWeek == DayOfWeek.SUNDAY })
        assertTrue(sunday.isRestDay)
        assertTrue(sunday.moves.all { it.phase == WorkoutPhase.COOL_DOWN })
    }

    @Test fun missingProfileFallsBackAndSaysSo() {
        val workout = G.generate(TrainingInputs(), monday)
        assertTrue(workout.moves.isNotEmpty())
        assertEquals(G.DEFAULT_MINUTES, workout.sessionMinutes)
        assertTrue(workout.basis[0].contains("not set"))
        assertTrue(workout.basis[1].contains("not set"))
        assertEquals(3, workout.basis.count { it.contains("default") })
        workout.moves.forEach { assertEquals(Equipment.BODYWEIGHT, it.exercise.equipment) }
    }

    @Test fun profileValuesAreCreditedToTheProfile() {
        val basis = plan(goal = FitnessGoal.GAIN_WEIGHT, days = 4).basis
        assertEquals("Goal: Gain Weight (from your profile)", basis[0])
        assertEquals("4 training days a week (from your profile)", basis[1])
        assertTrue(basis.none { it.contains("default") })
    }

    @Test fun sameInputsGiveTheSamePlan() {
        assertEquals(plan(), plan())
    }

    @Test fun prescriptionsReadNaturally() {
        val squat = PlannedMove(ExerciseLibrary.require("goblet_squat"), WorkoutPhase.MAIN, 3, reps = 10..12, restSeconds = 60)
        assertEquals("3 × 10–12 reps", squat.prescription)
        assertEquals("60 s rest between sets", squat.restText)
        assertEquals(3 * 36 + 2 * 60, squat.estimatedSeconds)
        val lunge = PlannedMove(ExerciseLibrary.require("forward_lunge"), WorkoutPhase.MAIN, 2, reps = 10..10, restSeconds = 45)
        assertEquals("2 × 10 reps each side", lunge.prescription)
        assertEquals(2 * 60 + 45, lunge.estimatedSeconds)
        val march = PlannedMove(ExerciseLibrary.require("march_in_place"), WorkoutPhase.WARM_UP, 1, seconds = 120)
        assertEquals("2 min", march.prescription)
        assertEquals(null, march.restText)
    }
}
