# Calfit UI redesign progress

Updated 2026-10-17. Experimental work is on local branch `ui-redesign` only.

## Implemented

- User-facing app label and visible auth, onboarding, coach, settings, and launch branding now read Calfit. Existing app ID, Kotlin package/namespace, Supabase identifiers, Room schema, and persistent preference keys remain unchanged.
- Added the AndroidX native SplashScreen API using the existing editable Calfit vector. The Compose intro uses a brief ring/wordmark reveal and waits for the existing auth restoration state before routing. It routes to Welcome, onboarding, or Home using the existing account state and runs once per splash composition.
- Refined Home’s calorie intake ring size and retained its real-data animation. Added real macro goal bars and activity summaries from the selected day’s saved exercise records.
- Refined Track with animated weekly workout-duration bars, weekly distance bars when saved distance history exists, and daily workout-time/distance values. The app has no step-count integration; Track explicitly reports that step tracking is not connected rather than displaying an invented zero or goal completion.
- Updated launcher adaptive icon background/foreground using the existing Calfit vector. No new brand artwork was fabricated.
- Profile behavior and nutrition persistence/calculation code were not changed. User-visible product wording on Profile-adjacent settings was updated for the Calfit rename only.

## Files changed

- `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/kcalfit/app/MainActivity.kt`: native splash integration and display label wiring.
- `app/src/main/java/com/kcalfit/app/ui/splash/SplashScreen.kt`: auth-aware Compose launch reveal.
- `app/src/main/java/com/kcalfit/app/ui/screens/HomeScreen.kt`, `ui/components/IntakeRing.kt`: Home data hierarchy and actual-value summaries.
- `app/src/main/java/com/kcalfit/app/ui/nakafit/TrackingScreens.kt`: real workout and distance visualizations, explicit step-source status.
- Auth, onboarding, chatbot, and settings copy: user-facing Calfit branding.
- `app/src/main/res/values/*`, `app/src/main/res/drawable/ic_launcher_{background,foreground}.xml`: Calfit display name, splash theme, and editable vector launcher art.
- `docs/ui-redesign-checklist.md`: audit checklist, execution and verification status.

## Checks

- `./gradlew assembleDebug testDebugUnitTest` — BUILD SUCCESSFUL; 33 unit tests passed, 0 failures.
- No Android UI test source set exists.
- No device/emulator was connected (`adb devices` returned an empty device list); no visual, navigation, auth-routing, theme, Profile, or food logging runtime test is claimed.
- Debug build: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub handoff blocker

The repository is `avaneeshtkur/Mad-Project-KcalFit`; fetched `origin/main` was at `952d829d7251255ba3e0aa6876e92dcd550b4f21` before local work. GitHub CLI is not authenticated, and local Git identity is `Rishi Moradia <rishimoradia033@gmail.com>`, not the requested `Rishimoradia03@gmail.com`. As required, no commit, push, or draft PR was created before the intended GitHub account and commit-email association can be verified. The local branch remains isolated as `ui-redesign`; `main` is unchanged.
