package com.kcalfit.app.ui.chatbot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/** Owned by one screen; all platform operations are performed on the main thread. */
class CoachVoiceController(private val context: Context,
    private val onTranscript: (String) -> Unit,
    private val onListening: (Boolean) -> Unit,
    private val onSpeaking: (Boolean) -> Unit,
    private val onError: (String) -> Unit) {
    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ready = false
    private var closed = false
    private var listening = false
    private var utterance: String? = null
    private val timeout = Runnable { if (listening) { cancelListening(); onError("Listening timed out. Tap the microphone to try again.") } }
    init {
        try { tts = TextToSpeech(context.applicationContext) { status -> main.post {
            if (!closed) {
                val result = if (status == TextToSpeech.SUCCESS) tts?.setLanguage(Locale.getDefault()) else null
                ready = result != null && result >= TextToSpeech.LANG_AVAILABLE
                if (!ready) onError("Spoken replies are unavailable. Check the device's text-to-speech language settings.")
            }
        } }
        } catch (_: Exception) { onError("No text-to-speech engine is available. Text chat still works.") }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) { main.post { if (!closed && id == utterance) onSpeaking(true) } }
            override fun onDone(id: String?) { main.post { if (!closed && id == utterance) { utterance = null; onSpeaking(false) } } }
            @Deprecated("Platform callback") override fun onError(id: String?) { main.post {
                if (!closed && id == utterance) { utterance = null; onSpeaking(false); onError("Couldn't speak this response. The text is still available.") }
            } }
        })
    }
    fun listen() {
        if (closed) return
        stopSpeaking()
        cancelListening()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) { onError("Speech recognition isn't installed. You can still type your message."); return }
        try {
            val session = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = session
            session.setRecognitionListener(object : RecognitionListener {
                fun active() = !closed && recognizer === session && listening
                override fun onReadyForSpeech(params: Bundle?) { }
                override fun onBeginningOfSpeech() { }
                override fun onRmsChanged(rmsdB: Float) { }
                override fun onBufferReceived(buffer: ByteArray?) { }
                override fun onEndOfSpeech() { }
                override fun onEvent(eventType: Int, params: Bundle?) { }
                override fun onPartialResults(results: Bundle?) {
                    if (active()) results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onTranscript)
                }
                override fun onResults(results: Bundle?) {
                    if (!active()) return
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    cancelListening()
                    if (text.isBlank()) onError("No speech detected. Try again or type a message.") else onTranscript(text)
                }
                override fun onError(error: Int) {
                    if (!active()) return
                    cancelListening()
                    onError(when(error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone access is denied. Enable it in app settings or type instead."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition needs a working connection. Try again or type instead."
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No clear speech detected. Please try again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "The microphone is busy. Please try again shortly."
                        else -> "Speech recognition is unavailable ($error). You can still type."
                    })
                }
            })
            listening = true; onListening(true)
            session.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)
            })
            main.postDelayed(timeout,30_000)
        } catch (_: Exception) { cancelListening(); onError("Couldn't start the microphone. Check permission or type instead.") }
    }
    fun cancelListening() {
        listening = false; onListening(false); main.removeCallbacks(timeout)
        val old = recognizer; recognizer = null
        old?.cancel(); old?.destroy()
    }
    fun speak(text: String) {
        cancelListening(); stopSpeaking()
        if (!ready || closed) { onError("Text-to-speech isn't ready. Check your installed voice and try Replay."); return }
        val id = UUID.randomUUID().toString(); utterance = id
        val limit = TextToSpeech.getMaxSpeechInputLength()
        val spoken = text.take(limit - 80) + if(text.length > limit - 80) ". Read the conversation for the rest of this response." else ""
        onSpeaking(true)
        if (tts?.speak(spoken,TextToSpeech.QUEUE_FLUSH,null,id) == TextToSpeech.ERROR) {
            utterance = null; onSpeaking(false); onError("Playback failed. You can still read the response.")
        }
    }
    fun stopSpeaking() { utterance = null; tts?.stop(); onSpeaking(false) }
    fun stop() { cancelListening(); stopSpeaking() }
    fun close() { closed = true; stop(); tts?.shutdown(); tts = null }
}
