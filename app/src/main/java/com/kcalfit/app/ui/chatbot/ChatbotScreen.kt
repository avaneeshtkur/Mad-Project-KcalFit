package com.kcalfit.app.ui.chatbot

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import com.kcalfit.app.domain.chatbot.CoachAction
import com.kcalfit.app.ui.viewmodel.CalFitUiState

@Composable
fun ChatbotScreen(uiState: CalFitUiState, onSendMessage: (String) -> Unit, busy: Boolean,
    action: CoachAction?, error: String?, onAction: (CoachAction) -> Unit, onBack: () -> Unit,
    modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var spokenReplies by rememberSaveable { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var awaitingReply by remember { mutableStateOf(false) }
    var sentAfterId by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val list = rememberLazyListState()
    val voice = remember(context) { CoachVoiceController(context,
        { text = it.take(2000) }, { listening = it }, { speaking = it }, { voiceError = it }) }
    DisposableEffect(voice, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { voice.stop(); awaitingReply = false }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); voice.close() }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        if (allowed && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) { spokenReplies = true; voice.listen() }
        else voiceError = "Microphone access is needed only for voice input. You can type, or enable access in app settings."
    }
    val latest = uiState.coachMessages.lastOrNull()
    LaunchedEffect(uiState.coachMessages.size, latest?.id, busy) {
        if(uiState.coachMessages.isNotEmpty()) list.animateScrollToItem(uiState.coachMessages.lastIndex)
        if (awaitingReply && !busy && latest?.sender == "COACH" && latest.id > sentAfterId) {
            awaitingReply = false
            if (spokenReplies && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) voice.speak(latest.messageText)
        }
        if (!busy && error != null) awaitingReply = false
    }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back") }
            Column(Modifier.weight(1f)) {
                Text("KcalFit Coach", style = MaterialTheme.typography.titleLarge)
                Text("Local assistant · your saved data", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { spokenReplies = !spokenReplies; if(!spokenReplies) voice.stopSpeaking() }) {
                Icon(if(spokenReplies) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    if(spokenReplies) "Disable spoken replies" else "Enable spoken replies", tint = MaterialTheme.colorScheme.primary)
            }
        }
        LazyColumn(Modifier.weight(1f), state = list, contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.coachMessages.isEmpty()) item {
                Text("Ask about today's meals, calories left, exercise technique, or where to find a feature. Food estimates use the local catalogue.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(uiState.coachMessages, key = { it.id }) { message ->
                val user = message.sender == "USER"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if(user) Arrangement.End else Arrangement.Start) {
                    Surface(Modifier.widthIn(max = 560.dp), shape = RoundedCornerShape(18.dp),
                        color = if(user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if(user) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) {
                        Column(Modifier.padding(14.dp)) {
                            Text(message.messageText, style = MaterialTheme.typography.bodyLarge)
                            if(!user) IconButton(onClick = { voice.speak(message.messageText) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp,"Read response aloud")
                            }
                        }
                    }
                }
            }
        }
        if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        action?.let { next -> TextButton(onClick = { voice.stop(); onAction(next) }) {
            Text("Open ${next.destination.name.lowercase().replace('_',' ')}")
        } }
        (voiceError ?: error)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        if (listening || speaking) Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if(listening) "Listening… review the transcript before sending" else "Speaking…", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { voice.stop() }) { Text("Stop") }
        }
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                voiceError = null
                if (listening) voice.cancelListening() else if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { spokenReplies = true; voice.listen() }
                else permission.launch(Manifest.permission.RECORD_AUDIO)
            }, enabled = !busy) { Icon(if(listening) Icons.Default.MicOff else Icons.Default.Mic,if(listening) "Cancel listening" else "Speak a message") }
            OutlinedTextField(text, { text = it.take(2000) }, Modifier.weight(1f),
                placeholder = { Text("Ask your coach") }, maxLines = 4, shape = RoundedCornerShape(20.dp),
                textStyle = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = {
                voice.stop(); sentAfterId = latest?.id ?: 0L; awaitingReply = true; onSendMessage(text); text = ""
            }, enabled = text.isNotBlank() && !busy && !listening) { Icon(Icons.AutoMirrored.Filled.Send,"Send message") }
        }
        Text("Voice recognition may use your device provider's online service.", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
    }
}
