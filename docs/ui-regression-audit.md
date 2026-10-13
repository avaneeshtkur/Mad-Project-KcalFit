# KcalFit UI regression audit

## Evidence reviewed

- `docs/recent-ui-polish.diff` and `docs/recent-ui-polish.md`
- `docs/nakafit-implementation-progress.md`
- Current `NavGraph`, `NakaComponents`, `HomeScreen`, `AddFoodScreen`, `SupportingScreens`, and `ChatbotScreen`
- Current Material 3 color schemes in `ui/theme/Color.kt` and `ui/theme/Theme.kt`
- Git history through commit `a81f1f5` and the current uncommitted implementation

The repository does not contain a clean pre-polish commit for the newer Nakafit screen set. Git history does contain an earlier KcalFit Home implementation with a calorie progress card, but it is architecturally different from the current Nakafit screen set. The raw recent diff is a hand-authored record rather than a machine-generated patch, so exact before/after line recovery is limited.

## Change classification

| Recent change | Classification | Finding |
|---|---|---|
| Move chatbot into the content layer | Correct, with a layout risk | Correctly prevents the shortcut from being trapped in the bottom bar. The bounded drag behavior is retained. |
| Force chatbot to a white light surface | Correct for the stated requirement | Kept, with explicit readable message and input colors. |
| Replace Workout Progress with calorie intake | Correct requirement | Kept. The current ring reports consumed calories against the daily goal. |
| Fixed dark Start Now buttons | Functionally correct; visually needs restraint | Kept high contrast, but constrained dimensions and spacing so the button does not dominate pastel cards. |
| Explicit Add Food text colors | Correct; previous implementation incomplete | Retained and extended with theme-aware field surfaces and borders. |
| Dark mode switch in notification settings | Correct requirement; duplicate settings control remains intentional | Kept because the user requested the control beside notification preferences. It still updates the existing persisted setting. |
| Theme-aware `NakaPage`/shared components | Visually inconsistent in the current screen set | Corrected shared controls first: outline, disabled, back-button, action, stat, and metric colors now use the active Material scheme. |

## Root causes corrected

1. Shared outlined buttons used hard-coded `Naka.Ink` content and `Naka.Line` borders, which produced poor contrast in dark mode.
2. Disabled buttons inherited Material defaults and could become pale or washed out against the app’s violet action system.
3. `NakaStat`, `NakaRow`, and `NakaSection` mixed fixed Nakafit colors with theme-aware page backgrounds.
4. Home metrics used hard-coded white surfaces and fixed dark text, breaking the intended surface hierarchy when the theme changed.
5. Food fields had readable text colors but no theme-aware container and border treatment, making them appear disconnected from the rest of the form.

## Files corrected

- `ui/nakafit/NakaComponents.kt`
- `ui/screens/HomeScreen.kt`
- `ui/screens/AddFoodScreen.kt`

The recent chatbot, notification, and navigation behavior remains intact.

## Verification

- `./gradlew assembleDebug` passed after the corrections.
- No emulator or screenshot comparison was run because the user previously instructed that emulator testing should be skipped.
- Remaining visual verification is manual device review of Home, Add Food, notification preferences, chatbot, and both theme states.
