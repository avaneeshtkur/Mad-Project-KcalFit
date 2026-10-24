package com.kcalfit.app.data.workout

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.kcalfit.app.CalFitApplication
import com.kcalfit.app.data.model.ExerciseEntry
import com.kcalfit.app.domain.workout.WorkoutAction
import com.kcalfit.app.domain.workout.WorkoutSessionState
import com.kcalfit.app.domain.workout.WorkoutSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Keeps the process alive during a live workout and refreshes its notification from
 * [WorkoutSessionTracker]. It owns no timer or calculation of its own and reads no sensors.
 */
class WorkoutSessionService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observer: Job? = null
    private var foreground = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = WorkoutSessionTracker.state.value as? WorkoutSessionState.Active
        val metrics = WorkoutSessionTracker.metrics(this)
        if (state == null || metrics == null) {
            // A startForegroundService() call must be answered with startForeground(), even if the
            // session was discarded before this ran; otherwise Android stops the app.
            if (!foreground && intent?.getBooleanExtra(EXTRA_FOREGROUND, false) == true) foreground = promote(WorkoutNotifications.ended(this))
            stop(); return START_NOT_STICKY
        }
        val notification = WorkoutNotifications.build(this, state.spec.workoutId, metrics)
        if (!foreground) foreground = promote(notification)
        if (!foreground) WorkoutNotifications.post(this, state.spec.workoutId, metrics)
        if (observer == null) observer = scope.launch { observe() }
        // The session is in memory only, so there is nothing to restore after the process dies.
        return START_NOT_STICKY
    }

    private fun promote(notification: Notification): Boolean {
        if (!WorkoutNotifications.canRunInForeground(this)) return false
        return try {
            ServiceCompat.startForeground(this, WorkoutNotifications.NOTIFICATION_ID, notification,
                if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH else 0)
            true
        } catch (_: Exception) {
            // SecurityException or a background-start restriction: keep a regular notification instead.
            false
        }
    }

    private suspend fun observe() {
        WorkoutSessionTracker.state.collectLatest { state ->
            if (state !is WorkoutSessionState.Active) { stop(); return@collectLatest }
            while (true) {
                WorkoutSessionTracker.metrics(this)?.let { WorkoutNotifications.post(this, state.spec.workoutId, it) }
                // The chronometer advances by itself; refreshes only bring calories and steps up to date.
                if (state.paused) break
                delay(REFRESH_MS)
            }
        }
    }

    private fun stop() {
        if (foreground) ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        foreground = false
        WorkoutNotifications.cancel(this)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        if (WorkoutSessionTracker.state.value !is WorkoutSessionState.Active) WorkoutNotifications.cancel(this)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_FOREGROUND = "foreground_start"
        private const val REFRESH_MS = 10_000L
    }
}

/** Notification Pause / Resume / End. Works from the shade and the lock screen while the app is in the background. */
class WorkoutActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = WorkoutAction.entries.firstOrNull { actionName(it) == intent.action } ?: return
        val app = context.applicationContext
        val summary = WorkoutSessionTracker.handle(app, action)
        val state = WorkoutSessionTracker.state.value
        if (state is WorkoutSessionState.Active) WorkoutSessionTracker.metrics(app)?.let { WorkoutNotifications.post(app, state.spec.workoutId, it) }
        // No session: it already ended, or the process restarted and the old notification is stale.
        else WorkoutNotifications.cancel(app)
        if (summary != null && summary.worthSaving) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try { save(app, summary) } finally { pending.finish() }
            }
        }
    }

    companion object {
        fun actionName(action: WorkoutAction) = "com.kcalfit.app.action.WORKOUT_${action.name}"

        /** Same entry the session screen's Save Workout creates. */
        suspend fun save(context: Context, summary: WorkoutSummary) {
            (context.applicationContext as CalFitApplication).repository.insertExerciseEntry(ExerciseEntry(
                exerciseName = summary.spec.title,
                category = summary.spec.category,
                durationMinutes = summary.minutes,
                caloriesBurned = summary.calories,
                date = summary.date
            ))
        }
    }
}
