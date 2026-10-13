# Voice mode

Use platform SpeechRecognizer and TextToSpeech, one controller per visible chat screen. Microphone permission is requested only on tap. Transcripts require explicit Send. Spoken replies are opt-in, with per-message replay and stop. Audio stops on background/disposal. No raw audio retained by KcalFit; the installed recognition provider may process audio online.

Platform references: https://developer.android.com/reference/android/speech/SpeechRecognizer and https://developer.android.com/reference/android/speech/tts/TextToSpeech . Android 11 service visibility queries are required for both services.

Physical microphone, provider availability, permission denial and TTS playback require device verification. Emulator execution excluded per user instruction.

## Implemented

- Manifest service queries for recognition and TTS; optional microphone hardware.
- CoachVoiceController owns recognizer and TTS, partial/final transcripts, a 30-second recognition timeout, errors, unique utterance IDs, interrupt/flush, and release.
- ChatbotScreen provides runtime permission, microphone/cancel, editable transcript, explicit send, spoken-response toggle, replay on each response, and Stop. Only a new reply after a send can autoplay; history never autoplays.
- Background and disposal cancel recognition and playback. Starting a microphone session stops speech. A disposed/background screen never starts recognition from a late permission result.
- Current device locale selected; missing voices/services and network errors leave typing available. Long utterances are bounded to the engine input limit with a spoken notice that remaining text is visible.
- Active app theme is used for the whole chat. This deliberately supersedes the former light-only chatbot to match the latest voice/theme requirement.

## Manual device checklist

Grant/deny/permanently deny microphone permission; retry from settings. Dictate, inspect partial/final text, edit and send. Start voice input (spoken replies turn on for the voice interaction), optionally turn speech off with the header control, replay a historical response, interrupt speech with microphone/Stop, and toggle spoken replies off. Background or navigate away mid-listen and mid-speech. Try an unavailable recognizer, missing TTS language and no network. Check keyboard, large text and both themes. No physical audio test has been performed in this session.
