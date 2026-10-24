package com.kcalfit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kcalfit.app.data.workout.WorkoutNotifications
import com.kcalfit.app.data.workout.WorkoutSessionTracker
import com.kcalfit.app.ui.navigation.NavGraph
import com.kcalfit.app.ui.theme.CalFitTheme
import com.kcalfit.app.ui.viewmodel.CalFitViewModel
import com.kcalfit.app.ui.viewmodel.CalFitViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: CalFitViewModel by viewModels {
        val app = application as CalFitApplication
        CalFitViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A recreated activity (rotation, process restore) or a relaunch from Recents re-delivers
        // the original intent; its auth link was already handled or has been consumed.
        val relaunched = savedInstanceState != null ||
            ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        if (!relaunched) handleAuthIntent(intent)
        if (!relaunched) handleWorkoutIntent(intent)
        enableEdgeToEdge()

        val app = application as CalFitApplication

        setContent {
            var darkMode by remember { mutableStateOf(app.userPreferences.isDarkMode()) }
            CalFitTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        userPreferences = app.userPreferences,
                        darkMode = darkMode,
                        onDarkModeChange = { enabled ->
                            app.userPreferences.setDarkMode(enabled)
                            darkMode = enabled
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
        handleWorkoutIntent(intent)
    }

    /** Tap on the live workout notification; navigation happens once the graph is ready. */
    private fun handleWorkoutIntent(intent: Intent?) {
        if (intent?.action != WorkoutNotifications.ACTION_OPEN_WORKOUT) return
        intent.getStringExtra(WorkoutNotifications.EXTRA_WORKOUT_ID)?.let(WorkoutSessionTracker::requestOpen)
    }

    private fun handleAuthIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        // The ViewModel outlives configuration changes, so processing continues if the activity is recreated.
        viewModel.handleAuthCallback(intent.data?.toString())
        intent.data = null
    }
}
