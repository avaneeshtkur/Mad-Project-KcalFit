package com.kcalfit.app.ui.reference

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.ui.auth.LoginScreen
import com.kcalfit.app.ui.navigation.Screen
import com.kcalfit.app.ui.theme.CalFitTheme
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequestedFlowsTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private fun nodes(node: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
        if (node == null) emptyList() else listOf(node) + (0 until node.childCount).flatMap { nodes(node.getChild(it)) }
    private fun all() = nodes(instrumentation.uiAutomation.rootInActiveWindow)
    private fun waitFor(label: String): AccessibilityNodeInfo {
        val deadline = System.currentTimeMillis() + 8000
        while (System.currentTimeMillis() < deadline) {
            all().firstOrNull { it.text?.toString() == label || it.contentDescription?.toString() == label }?.let { return it }
            Thread.sleep(100)
        }
        error("Missing screen element: $label")
    }
    private fun click(label: String) {
        var target = waitFor(label)
        while (!target.isClickable) target = requireNotNull(target.parent)
        assertTrue(target.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
    }
    private fun capture(name: String) {
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val directory = File(instrumentation.targetContext.filesDir, "requested-flows").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun dashboardAvatarAndCoachNavigation() {
        var route = ""
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { CalFitTheme { Surface {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    ReferenceDashboard(CalFitUiState(userProfile = UserEntity(name = "Avaneesh Thakur")), { route = it }, {}, onAddFood = {})
                }
            } } } }
            waitFor("A")
            waitFor("Notifications")
            capture("dashboard")
            click("Open chatbot")
            assertEquals(Screen.Chatbot.route, route)
        }
    }
    @Test fun signupKeepsGoogleAndRemovesApple() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { CalFitTheme {
                ReferenceSignup({ _, _, _ -> }, {}, {}, null, false)
            } } }
            waitFor("Continue with Google")
            assertFalse(all().any { it.text?.toString() == "Continue with Apple" })
            capture("signup")
        }
    }
    @Test fun loginCooldownDisablesSubmission() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { CalFitTheme {
                LoginScreen({ _, _ -> error("Login must be blocked") }, {}, {}, retryAfterSeconds = 60)
            } } }
            waitFor("Email address")
            assertFalse(all().any { it.text?.toString() in listOf("Use Saved Password", "Save with password manager") })
            val button = waitFor("Retry after 60 seconds")
            var target = button
            while (!target.isClickable && target.parent != null) target = target.parent
            assertFalse(target.isEnabled)
            capture("login")
        }
    }
}
