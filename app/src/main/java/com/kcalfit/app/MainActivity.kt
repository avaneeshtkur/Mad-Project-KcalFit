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
        enableEdgeToEdge()

        setContent {
            CalFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
