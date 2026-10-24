package com.kcalfit.app.domain.workout

/** A step-counter total for one local day, as written by the device step service. */
data class StepReading(val date: String, val count: Long)

data class WorkoutSessionSpec(
    val workoutId: String,
    val title: String,
    val category: String,
    val plannedMinutes: Int,
    val exercises: List<String>,
    val tracksSteps: Boolean,
    /** What to do for each entry of [exercises] (e.g. "3 × 10–12 reps"); empty when not planned. */
    val exerciseNotes: List<String> = emptyList()
)

/**
 * Steps counted only while the session is running. [baseline] is the day's step total when the
 * current running segment began; it is null while paused. Once a segment can't be measured (the
 * step source stopped or the day rolled over) the session's step figure is withdrawn for good.
 */
data class StepProgress(val accumulated: Long, val baseline: StepReading?, val reliable: Boolean = true)

sealed interface WorkoutSessionState {
    data object Idle : WorkoutSessionState
    data class Active(
        val spec: WorkoutSessionSpec,
        val startedDate: String,
        /** Running time banked by earlier segments. */
        val accumulatedMs: Long,
        /** Monotonic clock reading when the current segment began; null while paused. */
        val runningSince: Long?,
        val exerciseIndex: Int = 0,
        val steps: StepProgress? = null
    ) : WorkoutSessionState {
        val paused get() = runningSince == null
    }
}

data class WorkoutSummary(val spec: WorkoutSessionSpec, val elapsedSeconds: Int, val date: String) {
    val minutes get() = (elapsedSeconds / 60).coerceAtLeast(1)
    val calories get() = estimatedCalories(elapsedSeconds)
    /** A session finished before any time was recorded is ended without writing history. */
    val worthSaving get() = elapsedSeconds > 0
}

enum class WorkoutAction { PAUSE, RESUME, END }

/** The session screen's long-standing estimate (about 5 kcal per minute for every activity). */
fun estimatedCalories(elapsedSeconds: Int): Int = elapsedSeconds / 12

fun formatElapsed(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

/** Only metrics that are actually measured or estimated by the app; absent ones are null. */
data class LiveWorkoutMetrics(
    val title: String,
    val elapsedSeconds: Int,
    val paused: Boolean,
    val estimatedKcal: Int?,
    val steps: Long?,
    val exerciseNumber: Int?,
    val exerciseCount: Int?,
    val exerciseName: String?
)

/** Pure transitions so the screen, notification and tests share one set of rules. */
object WorkoutSessionMachine {

    fun start(state: WorkoutSessionState, spec: WorkoutSessionSpec, now: Long, date: String,
              reading: StepReading?): WorkoutSessionState =
        if (state is WorkoutSessionState.Active) state
        else WorkoutSessionState.Active(spec, date, 0L, now,
            steps = if (spec.tracksSteps && reading != null) StepProgress(0L, reading) else null)

    fun pause(state: WorkoutSessionState, now: Long, reading: StepReading?): WorkoutSessionState {
        if (state !is WorkoutSessionState.Active || state.runningSince == null) return state
        return state.copy(
            accumulatedMs = state.accumulatedMs + (now - state.runningSince).coerceAtLeast(0L),
            runningSince = null,
            steps = state.steps?.let { bank(it, reading) }
        )
    }

    fun resume(state: WorkoutSessionState, now: Long, reading: StepReading?): WorkoutSessionState {
        if (state !is WorkoutSessionState.Active || state.runningSince != null) return state
        return state.copy(runningSince = now, steps = state.steps?.let { progress ->
            if (progress.reliable && reading != null) progress.copy(baseline = reading)
            else progress.copy(baseline = null, reliable = false)
        })
    }

    fun nextExercise(state: WorkoutSessionState): WorkoutSessionState =
        if (state is WorkoutSessionState.Active && state.spec.exercises.isNotEmpty())
            state.copy(exerciseIndex = (state.exerciseIndex + 1) % state.spec.exercises.size)
        else state

    fun elapsedMs(state: WorkoutSessionState, now: Long): Long = when (state) {
        WorkoutSessionState.Idle -> 0L
        is WorkoutSessionState.Active -> state.accumulatedMs +
            (state.runningSince?.let { (now - it).coerceAtLeast(0L) } ?: 0L)
    }

    fun elapsedSeconds(state: WorkoutSessionState, now: Long): Int = (elapsedMs(state, now) / 1000).toInt()

    /** Summary for saving; the caller then moves the session to [WorkoutSessionState.Idle]. */
    fun summarize(state: WorkoutSessionState, now: Long): WorkoutSummary? =
        (state as? WorkoutSessionState.Active)?.let { WorkoutSummary(it.spec, elapsedSeconds(it, now), it.startedDate) }

    /** Steps taken while running, or null when there is no trustworthy, non-zero figure. */
    fun sessionSteps(state: WorkoutSessionState, reading: StepReading?): Long? {
        val progress = (state as? WorkoutSessionState.Active)?.steps ?: return null
        if (!progress.reliable) return null
        val live = progress.baseline?.let { segmentDelta(it, reading) ?: return null } ?: 0L
        return (progress.accumulated + live).takeIf { it > 0L }
    }

    fun metrics(state: WorkoutSessionState, now: Long, reading: StepReading?): LiveWorkoutMetrics? {
        val active = state as? WorkoutSessionState.Active ?: return null
        val seconds = elapsedSeconds(active, now)
        val exercises = active.spec.exercises
        val showExercise = exercises.size > 1
        return LiveWorkoutMetrics(
            title = active.spec.title,
            elapsedSeconds = seconds,
            paused = active.paused,
            estimatedKcal = estimatedCalories(seconds).takeIf { it > 0 },
            steps = sessionSteps(active, reading),
            exerciseNumber = if (showExercise) active.exerciseIndex + 1 else null,
            exerciseCount = if (showExercise) exercises.size else null,
            exerciseName = if (showExercise) exercises[active.exerciseIndex] else null
        )
    }

    /** Pause and End while running; Resume and End while paused; nothing without a session. */
    fun availableActions(state: WorkoutSessionState): List<WorkoutAction> =
        if (state is WorkoutSessionState.Active) actionsWhile(state.paused) else emptyList()

    fun actionsWhile(paused: Boolean): List<WorkoutAction> =
        if (paused) listOf(WorkoutAction.RESUME, WorkoutAction.END) else listOf(WorkoutAction.PAUSE, WorkoutAction.END)

    private fun bank(progress: StepProgress, reading: StepReading?): StepProgress {
        val baseline = progress.baseline ?: return progress
        val delta = segmentDelta(baseline, reading)
        return if (!progress.reliable || delta == null) progress.copy(baseline = null, reliable = false)
        else progress.copy(accumulated = progress.accumulated + delta, baseline = null)
    }

    private fun segmentDelta(baseline: StepReading, reading: StepReading?): Long? =
        if (reading == null || reading.date != baseline.date) null
        else (reading.count - baseline.count).coerceAtLeast(0L)
}
