package com.kcalfit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kcalfit.app.ui.auth.AuthViewModel
import com.kcalfit.app.ui.auth.AuthViewModelFactory
import com.kcalfit.app.ui.navigation.NavGraph
import com.kcalfit.app.ui.onboarding.OnboardingViewModel
import com.kcalfit.app.ui.onboarding.OnboardingViewModelFactory
import com.kcalfit.app.ui.theme.CalFitTheme
import com.kcalfit.app.ui.viewmodel.CalFitViewModel
import com.kcalfit.app.ui.viewmodel.CalFitViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: CalFitViewModel by viewModels {
        val app = application as CalFitApplication
        CalFitViewModelFactory(app.repository)
    }

    private val authViewModel: AuthViewModel by viewModels {
        val app = application as CalFitApplication
        AuthViewModelFactory(app.authRepository)
    }

    private val onboardingViewModel: OnboardingViewModel by viewModels {
        val app = application as CalFitApplication
        OnboardingViewModelFactory(app.repository, app.userPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CalFitApplication

        setContent {
            CalFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        onboardingViewModel = onboardingViewModel,
                        userPreferences = app.userPreferences
                    )
                }
            }
        }
    }
}
