package com.kcalfit.app.data.health
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
class HealthPrivacyActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{Surface{Column(Modifier.safeDrawingPadding().padding(24.dp)){Text("kcalfit health data",style=MaterialTheme.typography.headlineMedium);Text("With your permission, kcalfit reads step and distance totals from Health Connect to show your daily activity. Background access enables periodic updates. These totals stay on this device and are not sent to the food-image provider. You can revoke access in Health Connect or disconnect in More → Automatic steps.");TextButton({finish()}){Text("Close")}}}}}}}
