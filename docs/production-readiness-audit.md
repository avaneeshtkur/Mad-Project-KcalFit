# Production readiness audit

## Existing architecture and findings

- Native Compose, Navigation Compose, Room v5, one CalFitViewModel and FoodRepository, Supabase REST Auth. MainActivity owns one persisted theme flag. Keep this architecture.
- Real diary entries already use Room flows; a second in-memory preview diary in NavGraph bypasses persistence. Remove Explore and preview state.
- Only Breakfast opens a diary; other meal rows open Add Food directly. Home hides its existing delete callback. Track has no meal diary. Connect all four meals to one diary view and the same selected date.
- Detail lookup mixes saved entries with sample negative IDs, substitutes missing records with zero values and invents ingredient lists. Remove sample lookup/scan preview and render only the saved entry, with a missing-record state.
- Breakfast hard-codes white backgrounds. Dates are sorted historical dates rather than a week selector. Use actual LocalDate weeks with dates, historical week navigation and a date picker.
- Catalogue quantities are multiplied by servings only; manual quantity is fixed at one serving. Support quantities in the catalogue base unit and explicit manual nutrition for the entered portion. Reject nonfinite and negative values.
- Shared Naka components and several screens mix dark text and pastel/white surfaces with active Material colors. Correct paired foreground/surface colors and retain photographs/pastel exercise cards.
- Dark primary container is a legacy green and onPrimary is black on saturated violet. Correct purple tokens. Charts use pale alpha bars without axis labels.
- Refresh token is in ordinary SharedPreferences. Move it to AES-GCM storage protected by Android Keystore with one-time migration. Existing passwords are not persisted. Integrate Android Credential Manager save/retrieve, after successful Supabase login only.
- Startup refresh treats network failures as expired sessions. Distinguish invalid-token HTTP errors from network errors; preserve cached signed-in state offline.
- Room schema lacks a marker for unknown macros. Add a nondestructive v5→v6 migration and mark historical all-zero macro entries as unknown. New explicit zero macros remain valid.

## Phases

1. Remove Explore and app preview state/routes.
2. Shared food validation, quantity scaling, metadata and nondestructive migration; add/delete confirmation and mutation feedback.
3. Unified meal/date diary and stable saved-ID detail lookup; connect Home/Track/Nutrition/Goals.
4. Correct paired theme colors, Track metrics, charts and main Reminders theme shortcut.
5. Keystore session storage, Credential Manager convenience and sign-out.
6. Build, JVM calculation tests, source interaction checks and documented device limitations.

## Known limits

Food logs are local to this installation and the single active account; they are not synchronized to Supabase. Steps/distance have no sensor or Health Connect integration. Catalogue starter values are estimates, not a verified external nutrition service. Device pairing, subscriptions and camera recognition do not have production integrations. Preserve historical data; do not invent these services.

## Implemented corrections and interaction review

| Control / flow | Result |
| --- | --- |
| Welcome Explore | Removed, together with the in-memory demo state and sample meal fallback |
| Bottom tabs | Home, Track, Nutrition, Insight, Profile retained; system bottom inset stays applied once |
| Reminders / theme | Reminders opens existing preferences; adjacent sun/moon changes the single persisted MainActivity setting |
| Home calorie intake / Track Your Nutrition | Opens saved Breakfast diary; other meal rows pass their enum to `meal_diary/{meal}` |
| Home / Track / Daily Goal food records | Shared MealDiarySection uses selected-date Room records; Add action passes meal; food tap passes stable ID; Delete confirms and targets ID |
| Nutrition meal rows | All four open the same diary layout with their own meal parameter |
| Weekday / calendar | Seven unique Monday–Sunday dates for the selected week; previous/next week, Today and date picker preserve history |
| Add Food / manual nutrition | Quantity/unit included; catalogue values scale by entered amount/base serving; manual values represent the entire entered portion; finite nonnegative validation before save |
| Barcode result | Opens the quantity form for the selected catalogue item and meal; removes fabricated seed barcodes rather than accepting them as package IDs |
| Food detail | Resolves only the saved ID; displays stored quantity/calories/macros, with explicit unknown information; illustration clearly labelled |
| Save / delete errors | Save waits for Room success; failures remain in the form; delete errors use an error dialog |
| Saved date/meal | Saving changes the shared selected date to the entry's date and opens the entry's actual meal diary; cancellation keeps ordinary back behavior |
| History | Removed sample-history reload/seed actions; search/meal filters now flow into UI state and filter actual records; delete confirmation and back action added |
| Calorie charts | Correct theme primary/grid/surface colors, actual daily records, no false nonzero bars for empty periods |
| Workout Goal | Active days within the selected ISO week, using all saved exercise logs, against the existing days-per-week target |
| Steps / Distance | Equal sized responsive metric surfaces, no fabricated tracker values |
| Draggable chat | Relative position retained in NavGraph saved state; bounded to available content/keyboard area and adapts to rotation; existing chat route |
| Chatbot theme | Deliberately light as previously requested; local light Material scheme includes readable placeholder/border states |
| Login | Credential Manager retrieve and opt-in save after successful Supabase login, no password persistence in Compose saved-state bundles |
| Startup / sign-out | Encrypted Keystore refresh token; valid refresh routes to Home/onboarding; invalid token clears local auth; transient network failure keeps cached local access; sign-out clears provider/local state and attempts server revocation |

A restored protected navigation stack also observes the authoritative auth state and returns to Welcome when the session is invalid or signed out.

Credential Manager follows [Android's password guidance](https://developer.android.com/identity/passwords) using the stable [1.5.0 AndroidX release](https://developer.android.com/jetpack/androidx/releases/credentials#1.5.0). A configured password provider is needed on the target device. No separate password service or plaintext password store was introduced.

## Verification and remaining production work

- Build and JVM tests run with `./gradlew assembleDebug testDebugUnitTest`. Nine food-rule tests pass.
- `python3 scripts/test_food_migration.py` passes against SQLite and the actual Room-generated table definitions: v5→v6 schema equality, preserved historical IDs/quantities/macros, unknown-macro marker, synthetic barcode cleanup, deletion of one of two same-name entries and updated totals.
- `git diff --check` passes. Source inspection confirms no app preview flags/sample diary routes remain.
- No emulator/device rendering, Credential Manager provider prompt, Android Keystore runtime, live Supabase login or theme relaunch was exercised. The user's earlier no-emulator instruction remains in effect. These are device checks, not claimed as verified.
- The new nondestructive migration supports the current v5 schema. Databases predating v5 need an explicit schema-specific migration; destructive fallback has been removed.
- The existing single-active-account architecture clears local records when signing into a different Supabase account. This behavior was not converted to multi-account storage; switching accounts should not be used as a backup or synchronization mechanism. Same-account updates and date navigation preserve history.
- Existing local chatbot is a rule-based assistant, not an external AI API. Camera decoding, real device metrics/synchronization, payment processing and server food-log sync still need actual integrations.
- Legacy local-hash AuthRepository/viewmodels are unregistered code; the active NavGraph uses CalFitViewModel/SupabaseAuthService only. They are not a second enabled sign-in flow.
