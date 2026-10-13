package com.kcalfit.app.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.content.ContextCompat
import com.kcalfit.app.domain.home.*
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

@Composable
fun rememberLocalGreeting(): String {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var revision by remember { mutableIntStateOf(0) }
    var greeting by remember { mutableStateOf(greetingForHour(ZonedDateTime.now().hour)) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { revision++ }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED); addAction(Intent.ACTION_TIMEZONE_CHANGED); addAction(Intent.ACTION_DATE_CHANGED)
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    LaunchedEffect(lifecycle,revision) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while(true) {
                val now = ZonedDateTime.now()
                greeting = greetingForHour(now.hour)
                delay(millisUntilGreetingBoundary(now))
            }
        }
    }
    return greeting
}
