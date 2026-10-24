package com.kcalfit.app.domain.workout

import com.kcalfit.app.ui.exercise.ExerciseDemos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseLibraryTest {
    @Test fun idsAndNamesAreUnique() {
        assertEquals(ExerciseLibrary.all.size, ExerciseLibrary.all.map { it.id }.toSet().size)
        assertEquals(ExerciseLibrary.all.size, ExerciseLibrary.all.map { it.name.lowercase() }.toSet().size)
    }

    @Test fun everyExerciseHasInstructionsAndCues() {
        ExerciseLibrary.all.forEach {
            assertTrue(it.name, it.steps.isNotEmpty() && it.cues.isNotEmpty() && it.muscles.isNotEmpty())
            assertTrue(it.name, it.equipmentNote.isNotBlank())
        }
    }

    @Test fun everyReferencedAnimationIsBundled() {
        // Some bundled clips only stand in for related movements, so the library names a subset.
        assertTrue(ExerciseDemos.keys.containsAll(ExerciseLibrary.bundledDemos))
        ExerciseLibrary.all.forEach { exercise ->
            exercise.demo?.let { assertNotNull(exercise.name, ExerciseDemos.resourceFor(it)) }
        }
    }

    @Test fun unknownAnimationKeysHaveNoResource() {
        assertNull(ExerciseDemos.resourceFor("not_a_real_demo"))
    }

    @Test fun animationsAreOnlyAttachedToTheMovementTheyShow() {
        // The CDC lunge clip shows a forward lunge, and its push-up clip is against a wall.
        assertEquals("lunge", ExerciseLibrary.require("forward_lunge").demo)
        assertNull(ExerciseLibrary.require("reverse_lunge").demo)
        assertEquals("wallpushup", ExerciseLibrary.require("wall_push_up").demo)
        assertEquals("push_up", ExerciseLibrary.require("push_up").demo)
    }

    @Test fun existingWorkoutExerciseNamesResolve() {
        assertEquals("db_row", ExerciseLibrary.find("Dumbbell row")?.id)
        assertEquals("db_shoulder_press", ExerciseLibrary.find("Shoulder press")?.id)
        assertEquals("bodyweight_squat", ExerciseLibrary.find("  bodyweight SQUAT ")?.id)
        assertNull(ExerciseLibrary.find("Breathing practice"))
    }

    @Test fun trainingPlanNamesResolveToLibraryExercises() {
        mapOf("Bench Press" to "bench_press", "Push-ups" to "push_up", "Squats" to "barbell_back_squat",
            "Lunges" to "forward_lunge", "Calf Raises" to "calf_raise", "Overhead Press" to "db_shoulder_press",
            "Lat Pulldown" to "lat_pulldown", "Seated Cable Row" to "seated_cable_row", "Leg Press" to "leg_press",
            "Romanian Deadlift" to "db_romanian_deadlift", "Planks" to "plank", "Crunches" to "crunch",
            "Mountain Climbers" to "mountain_climbers", "Jumping Jacks" to "jumping_jacks", "Leg Curls" to "standing_hamstring_curl"
        ).forEach { (name, id) -> assertEquals(name, id, ExerciseLibrary.find(name)?.id) }
    }
}
