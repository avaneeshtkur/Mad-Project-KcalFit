package com.kcalfit.app.domain.workout

import com.kcalfit.app.domain.calculator.ActivityLevel
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.calculator.WorkoutPlanGenerator
import com.kcalfit.app.ui.exercise.ExerciseDemos
import com.kcalfit.app.ui.nakafit.nakaWorkouts
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseMediaTest {
    private fun assertBundled(label: String, match: DemoMatch) {
        assertTrue("$label has an empty animation key", match.key.isNotBlank())
        assertNotNull("$label → ${match.key} is not bundled", ExerciseDemos.resourceFor(match.key))
        assertNotNull("$label → ${match.key} has no credit", ExerciseMedia.credits[match.key])
    }

    @Test fun everyLibraryExerciseHasAnAnimation() {
        ExerciseLibrary.all.forEach { assertBundled(it.id, ExerciseMedia.forExercise(it)) }
    }

    @Test fun everyGeneratedDailyWorkoutExerciseHasAnAnimation() {
        val monday = LocalDate.of(2026, 10, 19)
        val seen = mutableSetOf<String>()
        for (goal in FitnessGoal.entries + listOf(null)) for (days in 1..7) for (experience in Experience.entries)
            for (equipment in Equipment.entries) for (minutes in DailyWorkoutGenerator.MINUTE_OPTIONS) for (offset in 0L..6L) {
                DailyWorkoutGenerator.generate(TrainingInputs(goal, days, experience, minutes, equipment), monday.plusDays(offset))
                    .moves.forEach { seen += it.exercise.id }
            }
        assertTrue(seen.size > 20)
        seen.forEach { assertBundled(it, ExerciseMedia.forExercise(ExerciseLibrary.require(it))) }
    }

    @Test fun everyWeeklyTrainingPlanExerciseHasAnAnimation() {
        val names = FitnessGoal.entries.flatMap { goal -> (1..7).flatMap { days ->
            WorkoutPlanGenerator.generatePlan(goal, days, ActivityLevel.MODERATELY_ACTIVE).flatMap { day -> day.exercises.map { it.name } }
        } }.toSet()
        assertTrue(names.isNotEmpty())
        names.forEach { assertBundled(it, ExerciseMedia.forName(it)) }
    }

    @Test fun everyPhotoWorkoutStepHasAnAnimation() {
        nakaWorkouts.flatMap { it.exercises }.forEach { assertBundled(it, ExerciseMedia.forName(it)) }
    }

    @Test fun unknownNamesFallBackToARelatedOrGenericAnimation() {
        assertEquals(DemoMatch("running", false), ExerciseMedia.forName("Interval jog"))
        assertEquals(DemoMatch(ExerciseMedia.GENERIC, false), ExerciseMedia.forName("Something brand new"))
        assertBundled("blank", ExerciseMedia.forName("   "))
    }

    @Test fun aliasesShareTheirExercisesAnimation() {
        assertEquals(ExerciseMedia.forName("Push-up"), ExerciseMedia.forName("  push-ups "))
        assertEquals(DemoMatch("push_up", true), ExerciseMedia.forName("Push-ups"))
        assertEquals(DemoMatch("pull_up", true), ExerciseMedia.forName("Pull-ups"))
    }

    @Test fun relatedClipsAreLabelledAsSuch() {
        assertFalse(ExerciseMedia.forExercise(ExerciseLibrary.require("reverse_lunge")).specific)
        assertFalse(ExerciseMedia.forExercise(ExerciseLibrary.require("plank")).specific)
        assertTrue(ExerciseMedia.forExercise(ExerciseLibrary.require("forward_lunge")).specific)
    }

    @Test fun everyBundledAnimationIsCreditedAndEveryCreditIsBundled() {
        assertEquals(ExerciseDemos.keys, ExerciseMedia.credits.keys)
        ExerciseMedia.credits.values.forEach {
            assertTrue(it.source, it.source.startsWith("https://commons.wikimedia.org/wiki/File:"))
            assertTrue(it.author.isNotBlank() && it.license.isNotBlank())
        }
    }

    @Test fun everyBundledFileIsAnAnimatedGif() {
        val raw = File("src/main/res/raw")
        ExerciseDemos.keys.forEach { key ->
            val file = File(raw, "demo_$key.gif")
            assertTrue("$file missing", file.isFile)
            assertTrue("$key is not an animated GIF", gifFrames(file.readBytes()) >= 2)
        }
    }

    /** Walks the GIF block structure and counts image frames; throws on a truncated or malformed file. */
    private fun gifFrames(b: ByteArray): Int {
        fun u(i: Int) = b[i].toInt() and 0xff
        check(String(b, 0, 6) in setOf("GIF87a", "GIF89a")) { "not a GIF" }
        check(u(6) or (u(7) shl 8) > 0 && u(8) or (u(9) shl 8) > 0) { "empty canvas" }
        var i = 13 + if (u(10) and 0x80 != 0) 3 * (2 shl (u(10) and 7)) else 0
        fun skipSubBlocks() { while (u(i) != 0) i += u(i) + 1; i++ }
        var frames = 0
        while (true) when (u(i)) {
            0x3B -> return frames
            0x21 -> { i += 2; skipSubBlocks() }
            0x2C -> {
                val packed = u(i + 9); i += 10
                if (packed and 0x80 != 0) i += 3 * (2 shl (packed and 7))
                i++; skipSubBlocks(); frames++
            }
            else -> error("bad block at $i")
        }
    }
}
