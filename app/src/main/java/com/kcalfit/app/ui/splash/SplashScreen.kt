package com.kcalfit.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.ui.navigation.Screen
import com.kcalfit.app.ui.components.CalFitLogo
import com.kcalfit.app.ui.nakafit.Naka
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

@Composable
fun SplashScreen(
    userProfile: UserEntity?,
    authReady: Boolean,
    onNavigateNext: (targetRoute: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.92f) }
    val alpha = remember { Animatable(0f) }
    val energy = remember { Animatable(0.62f) }
    val introComplete = remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(authReady, userProfile) {
        if (!introComplete.value) {
            coroutineScope {
                launch { scale.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
                launch { alpha.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }
                launch { energy.animateTo(.9f, tween(720, easing = FastOutSlowInEasing)) }
            }
            introComplete.value = true
        }
        if (!authReady) return@LaunchedEffect
        delay(180L)

        val targetRoute = when {
            userProfile == null || !userProfile.isLoggedIn -> Screen.Welcome.route
            !userProfile.isOnboarded -> Screen.Onboarding.route
            else -> Screen.Home.route
        }
        onNavigateNext(targetRoute)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Naka.Ink),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = Color.White.copy(alpha = .06f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { energy.value },
                    modifier = Modifier.size(100.dp).padding(4.dp),
                    color = Color(0xFF9C7BFF),
                    trackColor = Color.White.copy(alpha = .12f),
                    strokeWidth = 2.dp
                )
                CalFitLogo(size = 56.dp, showText = false)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Calfit",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Personal Fitness Tracker",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = .68f)
            )
        }
    }
}
