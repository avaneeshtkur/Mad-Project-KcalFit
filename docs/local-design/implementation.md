# Local MyFitnessPal export implementation

Source: `app/images/MYFITNESSPAL UI/MyFitnessPal application UI design (Community)/myfitnesspal ui/`.
Inspected every image on 2026-10-18. No Figma MCP used. `files.json` records every file, dimensions and aspect ratio. There are 30 PNGs: 27 single-screen frames and 3 component assets. No fonts, SVG, video, animation, 3D, README or machine-readable theme files were supplied. All nested directories are represented in the inventory.

## Baseline and architecture

`baseline-status.txt` and `baseline-staged.txt` record existing changes before UI edits. Existing staged exports, modified source, untracked food-search/tests, and deleted old reports are user/prior work, retained. A full tracked working-tree patch was saved at `/private/tmp/kcalfit-before-local-design.patch`. No history operations, commits, pushes or production deployments.

Native Compose navigation drives a Room-backed `CalFitViewModel`; Supabase Auth handles email/OAuth/session refresh; food search combines a local catalogue, Room cache and Open Food Facts transport/ranking. Existing manual logging, serving conversion, ID-based deletion, date filtering, workout, water, weight, chatbot, settings and profile paths remain. Reference screens are implemented around this state, without replacing database schemas or auth identifiers.

## Screen map and visual specification

All iOS files are 1179×2676 (0.4406); the bottom curation strip is outside the app. Native content is approximately 393×848 logical points before that strip. Android files are 360×800 (0.45). All are individual screens, not collages. Android system UI replaces the pictured iOS status bar, home indicator and keyboard.

| File (onboarding prefix abbreviated to iOS) | Purpose, hierarchy and interactions | Destination |
|---|---|---|
| iOS 0 | Solid blue splash, centered white heavy lowercase wordmark | SplashScreen |
| iOS 1 | Gray welcome, blue brand, rounded runner photo, bold caption, dots, blue signup pill, login | WelcomeScreen page 0 |
| iOS 2 | Same structure, food photo and macro artwork, third dot | WelcomeScreen page 1; middle page was not supplied |
| iOS 3 | Sign Up bar; centered welcome; rectangular continue/social buttons; privacy copy | ReferenceSignup intro |
| iOS 4 | Welcome bar, 7-segment progress, bold prompt, gray label, blue focused name field, keyboard | ReferenceSetup name/focused |
| iOS 5 | Same name field filled, bottom back circle and next pill | ReferenceSetup name/filled |
| iOS 6 | Goals bar/progress; greeting with hand; seven pale checkbox rows, bottom next | ReferenceSetup goals/unselected |
| iOS 7 | Same list, blue selected outline/check | ReferenceSetup goals/selected |
| iOS 8 | Blue gradient, white large encouragement at bottom, white next pill | ReferenceSetup encouragement |
| iOS 9 | Bold habits heading, recommended and other groups, wrapping outlined chips | ReferenceSetup habits/unselected |
| iOS 10 | Blue selected chip fills/outlines | ReferenceSetup habits/selected |
| iOS 11 | Blue gradient, orange-climbing illustration, large white message | ReferenceSetup habits confirmation |
| iOS 12 | Five pale radio rows, selected blue border, meal-planning frequency | ReferenceSetup planning |
| iOS 13 | Four descriptive activity rows; selected light activity | ReferenceSetup activity |
| iOS 14 | Sex radios, age, explanatory text, country selector, postal code | ReferenceSetup personal/empty |
| iOS 15 | Same personal form populated | ReferenceSetup personal/filled |
| iOS 16 | Imperial height two fields, weight and target; pale blue unit controls | ReferenceSetup measurements/imperial |
| iOS 17 | Metric height/weight/target fields empty | ReferenceSetup measurements/metric |
| iOS 18 | Metric fields populated | ReferenceSetup measurements/filled |
| iOS 19 | Weekly gain radio choices 0.2/0.5kg, recommended annotation | ReferenceSetup weekly goal |
| iOS 20 | Account title/progress, email/password, consent, terms/privacy, next | ReferenceSignup account |
| Android 15 | Account created; green calculated calorie goal, reminders/news checks, bottom next | ReferenceAccountCreated |
| Android 20 | Brand/profile/bell, yellow premium; Today/edit; calorie ring/card, steps/exercise, weight; blue search/nav | ReferenceDashboard standard |
| Android 21 | Premium variant with logging-progress arc, same calorie carousel and lower cards | ReferenceDashboard premium (verified entitlement only) |
| Android 22 | Diary/date arrows; calorie equation; fasting timer arc; meal headers/add-food rows; blue nav | ReferenceDiary |
| Android 23 | Nutrition tabs, daily date control, meal pie/legend, totals/net/goal, ranked foods | ReferenceNutrition Calories |
| Android 24 | Nutrition Macros, weekly date control, stacked chart/AVG, macro table, ranked foods | ReferenceNutrition Macros |

## Design tokens and assets

Palette labels give exact primary `#0066EE`, error `#DB471E`, success `#188977`, warning `#F5A524`. White, pale gray choice rows, gray outlines, pale blue selections, green progress, and chart colors are sampled/estimated from rendered PNGs; they are not recovered Figma tokens. Body/brand font metadata is unavailable. Android sans-serif with bold/extra-bold lowercase `kcalfit` is a permitted system-font approximation, not a confirmed match. No original wordmark is reused.

Static regions extracted from iOS 1/2 (welcome photography including embedded promotional chart artwork) and iOS 11 (illustration) preserve their original aspect ratios. They are illustrations, not full-screen backgrounds. Component-sheet icons are extracted into individual transparent PNGs. The supplied 24px export icon is reused. All real controls, text, charts and navigation are Compose. No 3D or animation source exists; no 3D recreation is claimed.

## Validation

Device and screenshot comparisons for every row are **pending user cross-check**, per the user's explicit instruction to stop emulator work. Source/build/host-test results and remaining configuration are recorded below as work completes. Visual acceptance is not inferred from compilation.

## Continuation — 2026-10-19

Compared the current implementation directly with local Android frames 20, 22, 23 and 24 and the supplied palette sheet. Continued the existing Compose implementation and reused the extracted kit icons.

- Nutrition: centered toolbar; one combined daily/weekly date control; pie percentages with small slices kept in the legend; bordered summary and ranked-food sections; macro grid lines, rounded scale, weekday/day labels and AVG; explicit gram and calorie-percentage headings. Weekly averages include all seven dates, including days without entries. Empty states and missing-macro notices remain. The nutrient table now includes units.
- Dashboard: calorie ring shows food, remaining base calories, and unused exercise credit; over-goal values use a positive magnitude with an “Over goal” label. The weight card reads the latest logged weight, and step progress stays within its display range.
- Shared header: titles stay centered even with multiple trailing actions. Bottom navigation retains the supplied curved blue background and kit icons, exposes selected tabs to accessibility, and separates the search and barcode touch targets.
- Added debug-only Android Studio previews in `app/src/debug/java/com/kcalfit/app/ui/reference/ReferenceScreensPreview.kt` for dashboard, diary, and populated/empty nutrition, including dark and large-text nutrition variants. Preview fixtures do not enter the application database.

No emulator or device was launched. Previews are available for inspection but were not rendered in this session; screenshot comparison and visual acceptance remain pending.

Validation for this continuation:
- `testDebugUnitTest`: 62 tests passed, 0 failures/errors/skips.
- `assembleDebug`: successful; final preview/navigation cleanup rebuilt afterward.
- `git diff --check`: passed.
- `lintDebug`: blocked by the existing `CredManMissingDal` error at `AndroidManifest.xml:26` (Credential Manager Digital Asset Links metadata is not configured). Report contains 96 warnings and 1 hint; no lint findings point to the four modified UI files. No lint suppression or placeholder domain was added.
