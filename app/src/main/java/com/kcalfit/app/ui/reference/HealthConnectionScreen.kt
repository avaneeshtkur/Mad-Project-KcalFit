package com.kcalfit.app.ui.reference

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import com.kcalfit.app.data.health.*
import com.kcalfit.app.ui.nakafit.*
import kotlinx.coroutines.launch

@Composable fun HealthConnectionScreen(onBack:()->Unit) {
    val context=LocalContext.current;val tracker=remember{StepTracking(context)};val scope=rememberCoroutineScope()
    var message by remember{mutableStateOf("Choose one step source. Health Connect combines compatible fitness apps without adding duplicate device counts.")}
    var revision by remember{mutableIntStateOf(0)}
    DisposableEffect(tracker) {
        val listener=SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        tracker.prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { tracker.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val source=tracker.prefs.getString("source","").orEmpty()
    val connected=source.isNotBlank()
    val health=rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()){granted->
        scope.launch{if(granted.containsAll(tracker.permissions)){tracker.useHealth();runCatching{tracker.sync()}.onFailure{message=it.message?:"Could not sync steps"};tracker.schedule();message="Health Connect connected. Background sync requires its separate permission."}else message="Step and distance access were not granted. You can retry or use the device sensor.";revision++}
    }
    fun startSensor(){try{ContextCompat.startForegroundService(context,Intent(context,StepCounterService::class.java));message="Device step counting started. Counts begin when the sensor reports its first baseline."}catch(e:Exception){message="Could not start device tracking: ${e.message}"}}
    val sensor=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)startSensor() else message="Activity recognition permission is needed to count device steps."}
    NakaPage("Automatic steps",onBack){
        Text(message)
        if (connected) {
            Text("Connected",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary)
            Text("Source: ${if(source=="health")"Health Connect" else "Device step sensor"}")
        } else {
            Text("Not connected",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Today: ${tracker.count()?.toString() ?: "No data"} steps")
        if (connected) {
            Text("Steps are being tracked automatically. Disconnect to choose a different source.",color=MaterialTheme.colorScheme.onSurfaceVariant)
            NakaButton("Disconnect",{tracker.stop();message="Tracking disconnected";revision++},outlined=true)
        } else {
            NakaButton("Connect Health Connect",{if(tracker.available)health.launch(tracker.permissions) else message="Health Connect is unavailable. Install or update Health Connect on a supported Android device."})
            if(tracker.backgroundAvailable())NakaButton("Allow background sync",{health.launch(tracker.permissions+HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)},outlined=true)
            NakaButton("Sync now",{scope.launch{message=runCatching{if(tracker.sync())"Steps updated" else "Connect Health Connect and grant access first"}.getOrElse{"Sync failed: ${it.message}"};revision++}},outlined=true)
            NakaButton("Use device step sensor",{
                if((context.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(Sensor.TYPE_STEP_COUNTER)==null)message="This device has no step-counter sensor. Use a compatible Health Connect data source."
                else if(Build.VERSION.SDK_INT>=29 && ContextCompat.checkSelfPermission(context,Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED)sensor.launch(Manifest.permission.ACTIVITY_RECOGNITION) else startSensor()
            },outlined=true)
            Text("Sensor totals cover periods when counting is enabled. Distance is shown only when supplied by Health Connect.")
        }
        Text("Connection updated $revision",style=MaterialTheme.typography.bodySmall)
    }
}
