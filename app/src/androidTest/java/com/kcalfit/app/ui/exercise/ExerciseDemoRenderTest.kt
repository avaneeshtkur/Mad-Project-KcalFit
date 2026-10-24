package com.kcalfit.app.ui.exercise

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.AnimatedImageDrawable
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inspector.WindowInspector
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.kcalfit.app.domain.calculator.ActivityLevel
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.calculator.WorkoutPlanGenerator
import com.kcalfit.app.domain.workout.ExerciseLibrary
import com.kcalfit.app.ui.workout.WorkoutPlanScreen
import com.kcalfit.app.ui.theme.CalFitTheme
import java.io.File
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Opens the real exercise sheets and checks on screen that the demonstration is drawn and moving:
 * a running animated drawable, and pixels in its area that change between two screenshots.
 * Screenshots are kept in the app's files/exercise-demos folder for a visual check. Uses a plain
 * ActivityScenario rather than the Compose test rule, whose Espresso can't start on Android 17 images.
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29)
class ExerciseDemoRenderTest {
    @Test fun cdcAnimationThatAlreadyExisted() = verify("bodyweight_squat") { Library("bodyweight_squat") }
    @Test fun newSpecificAnimation() = verify("push_up") { Library("push_up") }
    @Test fun twoFrameAnimation() = verify("lat_pulldown") { Library("lat_pulldown") }
    @Test fun relatedMovementAnimation() = verify("plank") { Library("plank") }
    @Test fun trainingPlanNameOutsideLibrary() = verify("dumbbell_flyes") { ExerciseDemoSheet("Dumbbell Flyes", listOf("3 sets × 8–12")) {} }
    @Test fun photoWorkoutStep() = verify("easy_run") { ExerciseDemoSheet("Easy run", listOf("Move at a comfortable pace.")) {} }

    /** The reported path: Training Plan → expand a day → tap an exercise. */
    @Test fun trainingPlanScreenOpensAnimatedDemo() = verify("training_plan_bench_press", before = {
        clickText("Day 1 - Push"); clickText("Bench Press")
    }) {
        WorkoutPlanScreen(WorkoutPlanGenerator.generatePlan(FitnessGoal.LOSE_WEIGHT, 3, ActivityLevel.MODERATELY_ACTIVE),
            FitnessGoal.LOSE_WEIGHT.name, 3, {}, {})
    }

    /** Taps the first on-screen element showing [text], as a user would, through the accessibility tree. */
    private fun clickText(text: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        // Compose doesn't answer findAccessibilityNodeInfosByText, so walk the tree.
        fun nodes(n: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
            if (n == null) emptyList() else listOf(n) + (0 until n.childCount).flatMap { nodes(n.getChild(it)) }
        val deadline = System.currentTimeMillis() + 8_000
        var node: AccessibilityNodeInfo? = null
        while (node == null && System.currentTimeMillis() < deadline) {
            node = nodes(automation.rootInActiveWindow).firstOrNull { it.text?.toString() == text }
            if (node == null) Thread.sleep(150)
        }
        var target: AccessibilityNodeInfo = requireNotNull(node) { "\"$text\" is not on screen" }
        while (!target.isClickable) target = requireNotNull(target.parent) { "\"$text\" is not clickable" }
        assertTrue(target.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        Thread.sleep(400)
    }

    @Composable private fun Library(id: String) = ExerciseDetailSheet(ExerciseLibrary.require(id), null) {}

    private fun verify(label: String, before: () -> Unit = {}, content: @Composable () -> Unit) = ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
        scenario.onActivity { it.setContent { CalFitTheme { content() } } }
        before()
        // The sheet animates in; wait until its demo view is laid out on screen.
        val deadline = System.currentTimeMillis() + 8_000
        while (onMain { demoView()?.takeIf { it.isShown && it.width > 0 } } == null && System.currentTimeMillis() < deadline) Thread.sleep(100)
        Thread.sleep(800)
        val (running, size, area) = onMain {
            val view = requireNotNull(demoView()) { "$label shows no animated demo" }
            val drawable = view.drawable as AnimatedImageDrawable
            val at = IntArray(2).also(view::getLocationOnScreen)
            Triple(drawable.isRunning, drawable.intrinsicWidth * drawable.intrinsicHeight, Rect(at[0], at[1], at[0] + view.width, at[1] + view.height))
        }
        assertTrue("$label animation is not running", running)
        assertTrue(size > 0)

        val first = screenshot()
        save(label, first)
        assertTrue("$label demo area is blank", distinctColours(first, area) > 4)
        // Two-frame clips hold each frame for about a second, so keep looking for up to three.
        val moved = (1..10).any { Thread.sleep(300); changedPixels(first, screenshot(), area) > 50 }
        assertTrue("$label demo did not move between screenshots", moved)
    }

    private fun <T> onMain(block: () -> T): T {
        var result: Result<T>? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = runCatching(block) }
        return result!!.getOrThrow()
    }

    private fun demoView(): ImageView? = WindowInspector.getGlobalWindowViews().flatMap(::imageViews)
        .firstOrNull { it.drawable is AnimatedImageDrawable && it.isShown }

    private fun imageViews(view: View): List<ImageView> = when (view) {
        is ImageView -> listOf(view)
        is ViewGroup -> (0 until view.childCount).flatMap { imageViews(view.getChildAt(it)) }
        else -> emptyList()
    }

    private fun screenshot(): Bitmap {
        val shot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        assertNotNull(shot)
        return shot!!.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun distinctColours(bitmap: Bitmap, area: Rect): Int {
        val colours = mutableSetOf<Int>()
        for (y in area.top until area.bottom step 6) for (x in area.left until area.right step 6) colours += bitmap.getPixel(x, y)
        return colours.size
    }

    private fun changedPixels(a: Bitmap, b: Bitmap, area: Rect): Int {
        var changed = 0
        for (y in area.top until area.bottom step 3) for (x in area.left until area.right step 3) if (a.getPixel(x, y) != b.getPixel(x, y)) changed++
        return changed
    }

    private fun save(label: String, bitmap: Bitmap) {
        val dir = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "exercise-demos").apply { mkdirs() }
        File(dir, "$label.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
