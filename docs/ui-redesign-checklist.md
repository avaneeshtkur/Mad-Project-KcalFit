# Calfit UI redesign checklist

Experimental branch: `ui-redesign` (testing only; do not merge or release from this branch).

## Audit and implementation checklist

- [x] Verify repository, clean worktree, `origin/main` base, and isolated feature branch.
- [x] Audit entry point, auth-gated navigation, Compose splash, themes, Home, Track, Nutrition, Profile, chatbot, Room persistence, Supabase auth, assets, and Gradle versions.
- [x] Apply user-facing Calfit branding while preserving app ID, package names, and stored preference/database names.
- [x] Refine Compose launch experience without changing auth routing.
- [x] Improve Home dashboard using existing Room totals and logged workouts.
- [x] Improve Track visualizations from actual activity history; do not synthesize steps or history.
- [x] Run the requested Gradle build and unit tests; device/UI checks are listed as unavailable below.
- [x] Produce a debug APK and record its path.
- [x] Review the final diff and document GitHub identity/remote limitations.

## Device and GitHub verification

- `./gradlew assembleDebug testDebugUnitTest`: passed (33 unit tests, 0 failures).
- `adb devices`: no connected emulator/device. UI interaction and visual verification have not been performed.
- No `app/src/androidTest` directory exists, so there are no automated UI tests to run.
- `gh auth status`: no GitHub account authenticated. Repository remote is `https://github.com/avaneeshtkur/Mad-Project-KcalFit.git`.
- Existing Git local identity is `Rishi Moradia <rishimoradia033@gmail.com>`, which differs from the requested commit email `Rishimoradia03@gmail.com`. The authenticated GitHub account and email association could not be verified, so no commits, push, or PR were created.
- Active branch is `ui-redesign`, based on fetched `origin/main` at `952d829d7251255ba3e0aa6876e92dcd550b4f21`. The remote `main` branch has not been modified.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Design direction

- Visual thesis: calm, premium training utility with ink-dark surfaces, crisp white type, and one restrained violet accent that still follows the existing light/dark theme.
- Content plan: orientation and calorie goal first; compact real activity and nutrition summaries next; existing diary and workout entry points remain the working detail.
- Interaction thesis: a short auth-aware launch reveal, animated calorie progress tied to real totals, and restrained activity bars that animate from saved records.

## Compatibility audit

- Navigation starts at `Splash` and waits for `authReady`; its current route decision preserves Welcome, onboarding, and authenticated Home behavior.
- Food logging and all dashboard totals are based on existing Room entries and remain untouched.
- Profile remains protected from redesign changes.
- No step sensor or step history source exists in this repository. Track must disclose this and visualize only recorded workout duration and distance.
- App ID, Kotlin namespace, Supabase configuration, Room schema, and persisted preference keys are compatibility-sensitive and will not be renamed for branding.

## Verification record

To be completed after implementation. Emulator/physical-device verification is recorded only if actually performed.
