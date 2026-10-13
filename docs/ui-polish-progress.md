# UI polish progress

Visual thesis: retain KcalFit's violet palette, calm surfaces and genuine photographs, with clearer calorie and food typography.
Content plan: existing greeting → calorie focal point → exercise photographs/actions → nutrition and diary; preserve all destinations.
Interaction thesis: animate only the calorie arc; use ordinary Material press feedback and explicit voice listening/speaking states.

Audit complete: Home greeting was hardcoded, ring was 66dp, exercise controls were compressed to 36dp, and chatbot used broad keyword responses under a forced light theme. No configured AI service or audio implementation was present. Changes below are implemented; device-only checks remain.

## Implemented batches

1. Chat: structured intents, date/meal/quantity context, Room-based personal answers, catalogue-only estimates, explicit navigation/review actions, bounded context, scoped backend interface with timeout/failure fallback. Removed unused estimated-food voice writes.
2. Voice: Android speech recognition and TTS controller; permission, partial/final transcript, explicit send, opt-in reply playback, replay/stop, failures and lifecycle cleanup. Theme follows app.
3. Home: calorie ring increased from 66dp to 112dp, 8dp stroke, 650ms arc animation, actual count stays accurate above goal. Goal/remaining/date labels retained; narrow/large-text layout stacks vertically. Images and three existing exercise destinations retained; full photographic cards with overlays and 48dp Start Now actions, adaptive footers.
4. Food typography: consistent title/body/label styles for entry rows and forms; clearer Serving/portion headings; compact responsive macro summaries and aligned detail values. Food persistence/calculation/meal selection preserved.
5. Greeting: pure hour function for 05:00/12:00/16:00/21:00 boundaries, actual profile name, resumed-lifecycle boundary timer, time/time-zone/date broadcast refresh. No background polling.

## Changed files in this pass

- domain/chatbot/FitnessChatbot.kt; domain/calculator/FoodDiaryRules.kt; new domain/home/HomePresentation.kt.
- ui/chatbot/ChatbotScreen.kt; new CoachVoiceController.kt.
- ui/components: new IntakeRing.kt and LocalGreeting.kt.
- ui/screens/HomeScreen.kt, AddFoodScreen.kt, NutritionScreen.kt, MealDetailScreen.kt; ui/nakafit/MealDiaryComponents.kt.
- ui/navigation/NavGraph.kt; ui/viewmodel/CalFitViewModel.kt; AndroidManifest.xml.
- New FitnessChatbotTest.kt and HomePresentationTest.kt; the three requested documents.

No image resources, authentication configuration, theme preferences, Gradle dependencies, database schema or exercise destinations were replaced in this pass.

## Verification boundaries

Intermediate debug builds and unit tests passed. SQLite migration/deletion regression script and whitespace checks passed. Final `assembleDebug testDebugUnitTest` passed; 33 JVM tests, zero failures. Voice input enables spoken replies for that interaction; the header control can disable them. `scripts/test_food_migration.py` and `git diff --check` passed. No emulator, screenshots, microphone, TTS playback, permission dialog or rendered-theme verification was performed, respecting the user's no-emulator instruction. Layout adaptation is implemented but still needs target-device review. The debug APK is at `app/build/outputs/apk/debug/app-debug.apk` (23 MB). The chatbot currently uses its local intent engine: a real LLM response service remains unavailable until an authenticated server endpoint/provider is configured.
