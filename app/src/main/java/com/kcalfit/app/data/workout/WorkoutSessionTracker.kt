package com.kcalfit.app.data.workout

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.kcalfit.app.data.health.StepTracking
import com.kcalfit.app.domain.workout.LiveWorkoutMetrics
import com.kcalfit.app.domain.workout.StepReading
import com.kcalfit.app.domain.workout.WorkoutAction
import com.kcalfit.app.domain.workout.WorkoutSessionMachine
import com.kcalfit.app.domain.workout.WorkoutSessionSpec
import com.kcalfit.app.domain.workout.WorkoutSessionState
import com.kcalfit.app.domain.workout.WorkoutSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * The one live workout session for this process. The session screen, the ongoing notification and
 * its actions all read and change this state; elapsed time is derived from a monotonic clock, so
 * there is no second timer to drift. The session lives in memory only and ends with the process.
 */
object WorkoutSessionTracker {
    private val _state = MutableStateFlow<WorkoutSessionState>(WorkoutSessionState.Idle)
    val state: StateFlow<WorkoutSessionState> = _state.asStateFlow()

    private val _openRequest = MutableStateFlow<String?>(null)
    /** Workout id the notification asked the UI to show; consumed by the navigation graph. */
    val openRequest: StateFlow<String?> = _openRequest.asStateFlow()

    fun now(): Long = SystemClock.elapsedRealtime()

    /** Returns false when another session is already active, so a second start never resets it. */
    fun start(context: Context, spec: WorkoutSessionSpec): Boolean {
        val started = synchronized(this) {
            if (_state.value is WorkoutSessionState.Active) return false
            _state.value = WorkoutSessionMachine.start(_state.value, spec, now(), LocalDate.now().toString(), stepReading(context))
            true
        }
        if (started) startService(context)
        return started
    }

    fun pause(context: Context) = update { WorkoutSessionMachine.pause(it, now(), stepReading(context)) }

    fun resume(context: Context) = update { WorkoutSessionMachine.resume(it, now(), stepReading(context)) }

    fun nextExercise() = update(WorkoutSessionMachine::nextExercise)

    /** Ends the session exactly once; repeated calls return null. The caller saves the summary. */
    fun end(context: Context): WorkoutSummary? = synchronized(this) {
        val summary = WorkoutSessionMachine.summarize(WorkoutSessionMachine.pause(_state.value, now(), stepReading(context)), now())
        _state.value = WorkoutSessionState.Idle
        summary
    }

    /** Throws the session away without saving (the screen's Reset). */
    fun discard() = synchronized(this) { _state.value = WorkoutSessionState.Idle }

    fun handle(context: Context, action: WorkoutAction): WorkoutSummary? = when (action) {
        WorkoutAction.PAUSE -> { pause(context); null }
        WorkoutAction.RESUME -> { resume(context); null }
        WorkoutAction.END -> end(context)
    }

    /** [now] lets a caller that ticks its own redraw clock read the same instant it recomposes on. */
    fun metrics(context: Context, now: Long = now()): LiveWorkoutMetrics? =
        WorkoutSessionMachine.metrics(_state.value, now, stepReading(context))

    /** Re-runs the service, e.g. once a permission granted after start lets it go foreground. */
    fun refreshService(context: Context) {
        if (_state.value is WorkoutSessionState.Active) startService(context)
    }

    fun requestOpen(workoutId: String) { _openRequest.value = workoutId }
    fun consumeOpenRequest() { _openRequest.value = null }

    private fun update(transition: (WorkoutSessionState) -> WorkoutSessionState) = synchronized(this) {
        _state.value = transition(_state.value)
    }

    private fun stepReading(context: Context): StepReading? {
        val date = LocalDate.now().toString()
        return StepTracking(context.applicationContext).liveSensorCount(date)?.let { StepReading(date, it) }
    }

    private fun startService(context: Context) {
        val app = context.applicationContext
        val foreground = WorkoutNotifications.canRunInForeground(app)
        val intent = Intent(app, WorkoutSessionService::class.java).putExtra(WorkoutSessionService.EXTRA_FOREGROUND, foreground)
        // A foreground start must be followed by startForeground(); only promise it when allowed.
        runCatching {
            if (foreground) ContextCompat.startForegroundService(app, intent) else app.startService(intent)
        }
    }
}
