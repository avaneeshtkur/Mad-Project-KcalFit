package com.kcalfit.app.data.workout

import com.kcalfit.app.domain.workout.LiveWorkoutMetrics
import com.kcalfit.app.domain.workout.WorkoutAction
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class WorkoutNotificationTextTest {
    private lateinit var locale: Locale
    @Before fun useUsLocale() { locale = Locale.getDefault(); Locale.setDefault(Locale.US) }
    @After fun restoreLocale() { Locale.setDefault(locale) }

    private val bare = LiveWorkoutMetrics("Yoga", 8, paused = false, estimatedKcal = null, steps = null,
        exerciseNumber = null, exerciseCount = null, exerciseName = null)

    @Test fun nothingIsInventedWhenNoMetricsExist() {
        assertEquals("Workout in progress", WorkoutNotificationText.collapsed(bare))
    }

    @Test fun onlySuppliedMetricsAppear() {
        val metrics = bare.copy(elapsedSeconds = 600, estimatedKcal = 50, steps = 1234,
            exerciseNumber = 2, exerciseCount = 3, exerciseName = "Main activity")
        assertEquals("1,234 steps · 50 kcal est.", WorkoutNotificationText.collapsed(metrics))
    }

    @Test fun pausedShowsTheFrozenDuration() {
        val metrics = bare.copy(elapsedSeconds = 754, paused = true, estimatedKcal = 62)
        assertEquals("12:34 · 62 kcal est.", WorkoutNotificationText.collapsed(metrics))
    }

    @Test fun collapsedLineLeadsWithTheMostImportantMetric() {
        // Narrow collapsed layouts truncate the end, so the measured figure must come first.
        assertEquals("1,234 steps · 50 kcal est.", WorkoutNotificationText.collapsed(bare.copy(estimatedKcal = 50, steps = 1234)))
        assertEquals("00:08 · 1,234 steps · 50 kcal est.",
            WorkoutNotificationText.collapsed(bare.copy(paused = true, estimatedKcal = 50, steps = 1234)))
    }

    @Test fun noPlaceholderTextLeaksIntoTheNotification() {
        val lines = listOf(WorkoutNotificationText.collapsed(bare.copy(estimatedKcal = 3)), WorkoutNotificationText.collapsed(bare))
        lines.forEach { line ->
            listOf("null", "N/A", "—", "0 steps").forEach { bad -> assertFalse("'$line' contains '$bad'", line.contains(bad)) }
        }
    }

    @Test fun everyActionHasADistinctBroadcast() {
        val names = WorkoutAction.entries.map(WorkoutActionReceiver::actionName)
        assertEquals(names.size, names.toSet().size)
        assertTrue(names.all { it.startsWith("com.kcalfit.app.action.") })
    }
}
