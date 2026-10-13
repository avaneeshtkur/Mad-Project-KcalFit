# Production readiness progress

Initial architecture audit complete. Findings and implementation phases are in production-readiness-audit.md.

## Completed batches

- Removed welcome Explore, the app-wide preview diary, sample meal fallback and scan-preview destination. Removed preview flags from Home/Track/Insight/Profile components.
- Added shared four-meal diary routes and inline entry lists on Home, Track and Daily Goals. Saved-ID details and confirmed individual deletion use Room. Save errors remain in the form; deletion errors are surfaced by navigation.
- Added portion quantity/unit and catalogue scaling, explicit manual macro availability, nonfinite/negative validation, and v5→v6 Room migration preserving entries. Legacy all-zero macros are marked unavailable; actual explicit zero macros remain valid.
- Added ordered Monday–Sunday dates and historical week/date navigation. Removed fixed Breakfast white canvas and fabricated food details.
- Main Reminders shortcut has the persisted theme toggle. Corrected purple dark tokens, shared surfaces, profile/device/forms and chart bars/gridlines/empty states. Steps/Distance are balanced and honestly unavailable without a data source.
- Added Android Credential Manager 1.5.0 retrieval and opt-in save after successful Supabase login; refresh token encrypted with Keystore AES-GCM in no-backup storage. Sign-out clears app session and provider state, and attempts remote revocation. Network failures no longer erase cached sessions.
- Added nine JVM tests for quantity scaling, same-name entry identity/totals, meal/date filtering, legacy Snacks names, week ordering and invalid nutrition/quantities. First full build and unit-test run passed.

## Final interaction corrections

- Restored authentication checks for a restored navigation stack: an invalid/cleared session returns protected destinations to Welcome. Offline cached access is retained only when an existing local signed-in profile remains.
- Removed sample-history controls, fixed History search and meal-filter state, and added History deletion confirmation/back navigation.
- On successful food save, the shared selected date follows the entry's date and navigation opens its actual meal diary, including if the user changed meal/date in the form. Cancelling still uses normal back navigation.
- Preserved the existing nutrition illustration in details with an honest label rather than suggesting the picture depicts the saved food.
- Import rejects rows with missing/invalid calorie values; incomplete macros and serving bases remain explicitly handled.

## Verification results

- `./gradlew assembleDebug testDebugUnitTest`: successful, nine tests, zero failures.
- `python3 scripts/test_food_migration.py`: successful. Validates the actual Room-generated schema against the v5→v6 SQL and checks historical preservation, ID-specific deletion and updated totals.
- `git diff --check`: successful.
- Source interaction audit recorded in production-readiness-audit.md. No live Supabase sign-in, password provider, Keystore runtime, emulator UI, theme restart or touch interaction was tested.

## Files changed in this pass

- Data: FoodEntry.kt, FoodItemEntity.kt, MealType.kt, FoodItemDao.kt, AppDatabase.kt, FoodRepository.kt, FoodAssetImporter.kt, UserPreferences.kt; new SecureSessionStore.kt.
- State/auth: CalFitViewModel.kt, AuthScreens.kt; new PasswordManager.kt.
- UI/navigation: NavGraph.kt, HomeScreen.kt, NutritionScreen.kt, MealDetailScreen.kt, AddFoodScreen.kt, HistoryScreen.kt, BarcodeLookupScreen.kt, NakaComponents.kt, NutritionScreens.kt, TrackingScreens.kt, SupportingScreens.kt, OnboardingScreen.kt, AddGoalsScreen.kt, Color.kt; new MealDiaryComponents.kt.
- Build/tests: app/build.gradle.kts, new FoodDiaryRules.kt and FoodDiaryRulesTest.kt, scripts/test_food_migration.py.
- Documentation: production-readiness-audit.md, this file, auth-and-food-setup.md, nakafit-implementation-progress.md.

## Exact next verification actions

On the target device: sign in and opt into provider saving, relaunch with a valid session, sign out and retrieve the saved credential; toggle the theme beside Reminders and restart; add two same-name portions in each meal/date, delete only one and inspect Home/Track/Nutrition/detail; check the migrated existing history; drag the chatbot and open the keyboard. Do not uninstall/clear app data to test the migration.

Production dependencies remain: validated nutrition catalogue, health/device integration, live Auth/password-provider checks, multi-account data ownership/sync, explicit migrations for databases older than v5, and actual visual review. The implementation is not declared fully production-ready.

Emulator execution was intentionally excluded by the user's earlier instruction. Device-specific Credential Manager, theme persistence and rendering checks remain separate from the passing build/data tests.
