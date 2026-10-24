package com.kcalfit.app.ui.exercise

import android.content.Context
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kcalfit.app.R
import com.kcalfit.app.domain.workout.DemoMatch
import com.kcalfit.app.domain.workout.ExerciseMedia
import com.kcalfit.app.domain.workout.LibraryExercise
import com.kcalfit.app.domain.workout.PlannedMove

/**
 * Every bundled exercise animation, keyed as in [ExerciseMedia]. All ship in the APK, so they need
 * no network; sources and licences are in ExerciseMedia.credits.
 */
object ExerciseDemos {
    private val raw = mapOf(
        "squat" to R.raw.demo_squat, "lunge" to R.raw.demo_lunge, "step_up" to R.raw.demo_step_up,
        "knee_curl" to R.raw.demo_knee_curl, "hip_abduction" to R.raw.demo_hip_abduction,
        "toe_stand" to R.raw.demo_toe_stand, "wallpushup" to R.raw.demo_wallpushup,
        "chest_press" to R.raw.demo_chest_press, "overhead_press" to R.raw.demo_overhead_press,
        "floor_back_extensions" to R.raw.demo_floor_back_extensions, "biceps_curl" to R.raw.demo_biceps_curl,
        "abdominal_curl" to R.raw.demo_abdominal_curl, "hamstring_stretch" to R.raw.demo_hamstring_stretch,
        "quad_stretch" to R.raw.demo_quad_stretch, "chest_stretch" to R.raw.demo_chest_stretch,
        "backstretch" to R.raw.demo_backstretch,
        "air_squat" to R.raw.demo_air_squat, "jumping_jacks" to R.raw.demo_jumping_jacks,
        "high_knees" to R.raw.demo_high_knees, "burpee" to R.raw.demo_burpee, "push_up" to R.raw.demo_push_up,
        "sit_up" to R.raw.demo_sit_up, "leg_raise" to R.raw.demo_leg_raise, "jump_rope" to R.raw.demo_jump_rope,
        "dumbbell_fly" to R.raw.demo_dumbbell_fly, "bench_press" to R.raw.demo_bench_press,
        "pull_up" to R.raw.demo_pull_up, "seated_row" to R.raw.demo_seated_row,
        "lat_pulldown" to R.raw.demo_lat_pulldown, "deadlift" to R.raw.demo_deadlift, "bridge" to R.raw.demo_bridge,
        "walking" to R.raw.demo_walking, "running" to R.raw.demo_running,
        "exercise_bike" to R.raw.demo_exercise_bike, "yoga" to R.raw.demo_yoga, "warm_up" to R.raw.demo_warm_up
    )

    @RawRes fun resourceFor(key: String?): Int? = key?.let(raw::get)

    val keys: Set<String> get() = raw.keys
}

/**
 * Loops the animation (ImageDecoder on Android 9+, Movie on Android 8). It holds still when the user
 * has turned animations off in system settings. If [res] can't be decoded it shows the generic
 * clip, and if even that fails, an icon, so the media area is never blank or broken.
 */
@Composable
fun ExerciseDemo(@RawRes res: Int, description: String, modifier: Modifier = Modifier) {
    // AndroidView builds its view once, so a different animation needs a fresh view.
    key(res) {
        AndroidView(
            factory = { context ->
                val fallback = ExerciseDemos.resourceFor(ExerciseMedia.GENERIC)
                (demoView(context, res) ?: fallback?.let { demoView(context, it) } ?: placeholderView(context))
                    .apply { contentDescription = description }
            },
            modifier = modifier
        )
    }
}

private fun animationsOn(context: Context) =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

private fun demoView(context: Context, @RawRes res: Int): View? = if (Build.VERSION.SDK_INT >= 28) {
    runCatching { ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.resources, res)) }.getOrNull()?.let { drawable ->
        if (animationsOn(context)) (drawable as? AnimatedImageDrawable)?.apply { repeatCount = AnimatedImageDrawable.REPEAT_INFINITE }?.start()
        ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(drawable) }
    }
} else {
    @Suppress("DEPRECATION")
    runCatching { context.resources.openRawResource(res).use(Movie::decodeStream) }.getOrNull()
        ?.takeIf { it.width() > 0 && it.height() > 0 }?.let { MovieView(context, it, animationsOn(context)) }
}

private fun placeholderView(context: Context) = ImageView(context).apply {
    scaleType = ImageView.ScaleType.CENTER_CROP
    setImageResource(R.drawable.nakafit_gym_motivation)
}

/** Android 8 has no AnimatedImageDrawable; Movie draws the GIF frame for the current time instead. */
@Suppress("DEPRECATION")
private class MovieView(context: Context, private val movie: Movie, private val animate: Boolean) : View(context) {
    private val start = SystemClock.uptimeMillis()

    override fun onDraw(canvas: Canvas) {
        val duration = movie.duration().takeIf { it > 0 } ?: 1000
        movie.setTime(if (animate) ((SystemClock.uptimeMillis() - start) % duration).toInt() else 0)
        val scale = minOf(width / movie.width().toFloat(), height / movie.height().toFloat())
        canvas.save()
        canvas.translate((width - movie.width() * scale) / 2, (height - movie.height() * scale) / 2)
        canvas.scale(scale, scale)
        movie.draw(canvas, 0f, 0f)
        canvas.restore()
        if (animate) postInvalidateOnAnimation()
    }
}

/** The animation for [match], with its credit, and a note when it shows a related movement. */
@Composable
fun ExerciseDemoPanel(match: DemoMatch, exerciseName: String) {
    val res = ExerciseDemos.resourceFor(match.key) ?: ExerciseDemos.resourceFor(ExerciseMedia.GENERIC)
    val credit = ExerciseMedia.credits[match.key]
    Column {
        // A fixed height: aspectRatio with a max height overflowed onto the text around it on wide phones.
        val area = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(18.dp)).background(Color.White)
        if (res != null) ExerciseDemo(res, "Animation demonstrating ${if (match.specific) exerciseName else "a movement similar to $exerciseName"}", area)
        else Box(area, contentAlignment = Alignment.Center) { Icon(Icons.Default.FitnessCenter, null, tint = Color.Gray) }
        if (!match.specific) Text("Shows a similar movement; follow the steps for this exercise.", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        credit?.let { Text("Animation: ${it.author} · ${it.license} · Wikimedia Commons", fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp)) }
    }
}

/** Exercise detail: shown from today's workout, a workout's exercise list and the live session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailSheet(exercise: LibraryExercise, move: PlannedMove?, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(exercise.name, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (move != null) {
                Text(move.prescription, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                move.restText?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            ExerciseDemoPanel(ExerciseMedia.forExercise(exercise), exercise.name)
            DetailLine("Muscles", exercise.muscles.joinToString { it.label })
            DetailLine("Equipment", exercise.equipmentNote)
            Text("How to do it", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            exercise.steps.forEachIndexed { i, step ->
                Text("${i + 1}. $step", color = MaterialTheme.colorScheme.onSurface)
            }
            Text("Form cues", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            exercise.cues.forEach { cue -> Text("• $cue", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text("Stop if you feel sharp pain, dizziness or chest discomfort.", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** For plan exercises that aren't in the library: the animation plus the plan's own notes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDemoSheet(name: String, notes: List<String>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(name, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            ExerciseDemoPanel(ExerciseMedia.forName(name), name)
            notes.forEach { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text("Stop if you feel sharp pain, dizziness or chest discomfort.", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "$label: $value" }) {
        Text(label, Modifier.widthIn(min = 96.dp).padding(end = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
    }
}
