package com.kcalfit.app.data.workout

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kcalfit.app.MainActivity
import com.kcalfit.app.R
import com.kcalfit.app.domain.workout.LiveWorkoutMetrics
import com.kcalfit.app.domain.workout.WorkoutAction
import com.kcalfit.app.domain.workout.WorkoutSessionMachine
import com.kcalfit.app.domain.workout.formatElapsed
import java.text.NumberFormat

/** Text for the live notification. Each part exists only when its metric does. */
object WorkoutNotificationText {
    fun kcal(value: Int) = "$value kcal est."
    fun steps(value: Long) = "${NumberFormat.getIntegerInstance().format(value)} steps"

    /**
     * One line, most important first, because the collapsed layout (narrowed by its action buttons)
     * cuts the end off: the frozen duration while paused (the header says "Paused"; while running
     * its chronometer shows the time), then measured steps, then estimated kcal.
     */
    fun collapsed(metrics: LiveWorkoutMetrics): String {
        val parts = listOfNotNull(
            if (metrics.paused) formatElapsed(metrics.elapsedSeconds) else null,
            metrics.steps?.let(::steps),
            metrics.estimatedKcal?.let(::kcal)
        )
        return parts.joinToString(" · ").ifEmpty { "Workout in progress" }
    }
}

object WorkoutNotifications {
    const val CHANNEL_ID = "workout_session"
    const val NOTIFICATION_ID = 2102
    const val ACTION_OPEN_WORKOUT = "com.kcalfit.app.action.OPEN_WORKOUT_SESSION"
    const val EXTRA_WORKOUT_ID = "workout_id"

    /** Android 14+ only grants a health foreground service to apps holding a health runtime permission. */
    fun canRunInForeground(context: Context): Boolean = Build.VERSION.SDK_INT < 34 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    fun notificationsEnabled(context: Context): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannel(CHANNEL_ID)
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Live workout", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Shows the workout you are tracking, with pause, resume and finish controls."
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * The standard template only shows actions once expanded, so this uses the media template,
     * whose compact actions stay visible collapsed. No session token is attached, so the system
     * treats it as an ordinary notification rather than a media player. No custom views.
     */
    fun build(context: Context, workoutId: String, metrics: LiveWorkoutMetrics): Notification {
        ensureChannel(context)
        val actions = WorkoutSessionMachine.actionsWhile(metrics.paused)
        val publicVersion = base(context, workoutId, metrics, actions)
            .setContentTitle(if (metrics.paused) "Workout paused" else "Workout in progress")
            .setContentText(if (metrics.paused) "Paused at ${formatElapsed(metrics.elapsedSeconds)}" else "kcalfit")
            .build()
        return base(context, workoutId, metrics, actions)
            .setContentTitle(metrics.title)
            .setContentText(WorkoutNotificationText.collapsed(metrics))
            .setSubText(if (metrics.paused) "Paused" else null)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .build()
    }

    /** Posts or refreshes the notification; silently skipped when the user has blocked notifications. */
    fun post(context: Context, workoutId: String, metrics: LiveWorkoutMetrics) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, build(context, workoutId, metrics)) }
    }

    /** Placeholder used only to satisfy a pending foreground start for a session that already ended. */
    fun ended(context: Context): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.phosphor_barbell_regular)
            .setContentTitle("Workout ended")
            .setSilent(true)
            .build()
    }

    fun cancel(context: Context) = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    private fun base(context: Context, workoutId: String, metrics: LiveWorkoutMetrics, actions: List<WorkoutAction>) =
        Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.phosphor_barbell_regular)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent(context, workoutId))
            .apply {
                if (Build.VERSION.SDK_INT >= 31) setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                if (metrics.paused) setShowWhen(false).setUsesChronometer(false)
                else setShowWhen(true).setUsesChronometer(true)
                    .setWhen(System.currentTimeMillis() - metrics.elapsedSeconds * 1000L)
                actions.forEach { action ->
                    addAction(Notification.Action.Builder(Icon.createWithResource(context, icon(action)), label(action),
                        actionIntent(context, action)).build())
                }
                style = Notification.MediaStyle().setShowActionsInCompactView(*actions.indices.toList().toIntArray())
            }

    private fun label(action: WorkoutAction) = when (action) {
        WorkoutAction.PAUSE -> "Pause"
        WorkoutAction.RESUME -> "Resume"
        WorkoutAction.END -> "Finish"
    }

    private fun icon(action: WorkoutAction) = when (action) {
        WorkoutAction.PAUSE -> R.drawable.ic_workout_pause
        WorkoutAction.RESUME -> R.drawable.ic_workout_resume
        WorkoutAction.END -> R.drawable.ic_workout_finish
    }

    private fun openIntent(context: Context, workoutId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_OPEN_WORKOUT)
            .putExtra(EXTRA_WORKOUT_ID, workoutId)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun actionIntent(context: Context, action: WorkoutAction): PendingIntent {
        val intent = Intent(context, WorkoutActionReceiver::class.java).setAction(WorkoutActionReceiver.actionName(action))
        return PendingIntent.getBroadcast(context, action.ordinal + 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
