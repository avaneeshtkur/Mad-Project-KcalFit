package com.kcalfit.app.domain.workout

import com.kcalfit.app.domain.workout.WorkoutSessionMachine as M
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSessionMachineTest {
    private val day = "2026-10-24"
    private val walk = WorkoutSessionSpec("activity_walking", "Walking", "Cardio", 20,
        listOf("Warm up", "Main activity", "Cool down"), tracksSteps = true)
    private val lift = walk.copy(workoutId = "weights", title = "Lift", tracksSteps = false)

    private fun active(state: WorkoutSessionState) = state as WorkoutSessionState.Active

    @Test fun startRunsFromZero() {
        val state = active(M.start(WorkoutSessionState.Idle, lift, 1_000, day, null))
        assertEquals(0, M.elapsedSeconds(state, 1_000))
        assertEquals(5, M.elapsedSeconds(state, 6_000))
        assertTrue(!state.paused)
    }

    @Test fun secondStartDoesNotReplaceTheRunningSession() {
        val first = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        assertSame(first, M.start(first, walk, 50_000, day, null))
    }

    @Test fun pauseFreezesTimeAndResumeKeepsIt() {
        var state = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        state = M.pause(state, 30_000, null)
        assertEquals(30, M.elapsedSeconds(state, 90_000))
        state = M.resume(state, 90_000, null)
        assertEquals(40, M.elapsedSeconds(state, 100_000))
    }

    @Test fun repeatedPauseOrResumeIsHarmless() {
        var state = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        state = M.pause(M.pause(state, 10_000, null), 20_000, null)
        assertEquals(10, M.elapsedSeconds(state, 99_000))
        state = M.resume(M.resume(state, 30_000, null), 40_000, null)
        assertEquals(20, M.elapsedSeconds(state, 40_000))
    }

    @Test fun summaryUsesTheExistingMinutesAndCalorieRules() {
        val running = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        val summary = M.summarize(running, 125_000)!!
        assertEquals(125, summary.elapsedSeconds)
        assertEquals(2, summary.minutes)
        assertEquals(125 / 12, summary.calories)
        assertEquals(day, summary.date)
        assertEquals(1, M.summarize(running, 10_000)!!.minutes)
    }

    @Test fun nothingToSummarizeOnceIdle() {
        assertNull(M.summarize(WorkoutSessionState.Idle, 5_000))
        assertNull(M.metrics(WorkoutSessionState.Idle, 5_000, null))
    }

    @Test fun actionsFollowTheSessionState() {
        val running = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        assertEquals(listOf(WorkoutAction.PAUSE, WorkoutAction.END), M.availableActions(running))
        assertEquals(listOf(WorkoutAction.RESUME, WorkoutAction.END), M.availableActions(M.pause(running, 1_000, null)))
        assertTrue(M.availableActions(WorkoutSessionState.Idle).isEmpty())
    }

    @Test fun nextExerciseWraps() {
        var state = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        repeat(3) { state = M.nextExercise(state) }
        assertEquals(0, active(state).exerciseIndex)
    }

    @Test fun stepsCountOnlyRunningSegments() {
        var state = M.start(WorkoutSessionState.Idle, walk, 0, day, StepReading(day, 1_000))
        assertEquals(150L, M.sessionSteps(state, StepReading(day, 1_150)))
        state = M.pause(state, 60_000, StepReading(day, 1_150))
        // Steps taken while paused are ignored.
        state = M.resume(state, 120_000, StepReading(day, 1_400))
        assertEquals(250L, M.sessionSteps(state, StepReading(day, 1_500)))
    }

    @Test fun noStepsWithoutALiveSourceOrForNonStepActivities() {
        val noSource = M.start(WorkoutSessionState.Idle, walk, 0, day, null)
        assertNull(M.sessionSteps(noSource, StepReading(day, 500)))
        val lifting = M.start(WorkoutSessionState.Idle, lift, 0, day, StepReading(day, 100))
        assertNull(M.sessionSteps(lifting, StepReading(day, 900)))
    }

    @Test fun zeroStepsAreHiddenRatherThanShownAsZero() {
        val state = M.start(WorkoutSessionState.Idle, walk, 0, day, StepReading(day, 1_000))
        assertNull(M.sessionSteps(state, StepReading(day, 1_000)))
    }

    @Test fun stepsAreWithdrawnWhenTheyCannotBeMeasured() {
        val started = M.start(WorkoutSessionState.Idle, walk, 0, day, StepReading(day, 1_000))
        assertNull(M.sessionSteps(started, StepReading("2026-10-25", 20)))
        val pausedAcrossMidnight = M.pause(started, 1_000, StepReading("2026-10-25", 20))
        assertNull(M.sessionSteps(M.resume(pausedAcrossMidnight, 2_000, StepReading("2026-10-25", 30)), StepReading("2026-10-25", 90)))
        val sourceStopped = M.resume(M.pause(started, 1_000, StepReading(day, 1_100)), 2_000, null)
        assertNull(M.sessionSteps(sourceStopped, StepReading(day, 1_300)))
    }

    @Test fun metricsOmitWhatIsNotAvailable() {
        val single = lift.copy(exercises = listOf("Main activity"))
        val state = M.start(WorkoutSessionState.Idle, single, 0, day, null)
        val early = M.metrics(state, 5_000, null)!!
        assertNull(early.estimatedKcal)
        assertNull(early.steps)
        assertNull(early.exerciseNumber)
        assertNull(early.exerciseName)
        assertEquals(5, M.metrics(state, 60_000, null)!!.estimatedKcal)
    }

    @Test fun metricsReportPlanProgress() {
        val state = M.nextExercise(M.start(WorkoutSessionState.Idle, lift, 0, day, null))
        val metrics = M.metrics(M.pause(state, 24_000, null), 99_000, null)!!
        assertTrue(metrics.paused)
        assertEquals(24, metrics.elapsedSeconds)
        assertEquals(2, metrics.exerciseNumber)
        assertEquals(3, metrics.exerciseCount)
        assertEquals("Main activity", metrics.exerciseName)
    }

    @Test fun finishIsOfferedFromTheFirstMomentWhetherRunningOrPaused() {
        val started = M.start(WorkoutSessionState.Idle, lift, 5_000, day, null)
        assertEquals(0, M.elapsedSeconds(started, 5_000))
        assertTrue(WorkoutAction.END in M.availableActions(started))
        assertTrue(WorkoutAction.END in M.availableActions(M.pause(started, 5_000, null)))
    }

    @Test fun aSessionWithNoRecordedTimeIsNotSaved() {
        val started = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        assertTrue(!M.summarize(started, 0)!!.worthSaving)
        assertTrue(M.summarize(started, 1_000)!!.worthSaving)
    }

    @Test fun longPausesDoNotCount() {
        var state = M.start(WorkoutSessionState.Idle, lift, 0, day, null)
        state = M.pause(state, 90_000, null)
        // Locked phone for an hour while paused.
        state = M.resume(state, 3_690_000, null)
        assertEquals(100, M.elapsedSeconds(state, 3_700_000))
    }
}
