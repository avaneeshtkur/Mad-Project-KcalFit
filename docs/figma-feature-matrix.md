# Nakafit feature matrix

Status: **existing** means present in project before this pass; **planned** means inferred from a viewed Figma PNG; **blocked** means design/backend detail is unavailable. Verification is separate from implementation.

| Feature | Source | Expected behavior | Data/UI needs | Implementation | Verification |
| --- | --- | --- | --- | --- | --- |
| Auth/session | Existing app; no auth frame on cover | Supabase email sign-up, OTP, sign-in, recovery, session refresh | Existing REST service and preferences | existing | build pending; network untested |
| Onboarding/profile | 2513:31–37, 2513:58 | Configure user and measurements | Existing Room profile + Supabase metadata | existing; Figma visuals inaccessible | pending |
| Home dashboard | 2513:22 | Live progress, overview, exercise navigation | Existing entries/goals + Compose cards | existing; visual restyle planned | pending |
| Add goals | 2513:7 | Validate and save activity/repeat/reminder | New local goal model/repository or extend existing | planned | pending |
| Food diary/date list | 2513:9–10 | Browse daily meals, add entries, calculate macros | Existing Room food entries and search | existing, design adaptation planned | pending |
| Meal detail | 2513:17 | Show calories/macros/ingredients | Existing food records; ingredient data unavailable | planned | pending |
| Barcode scan | 2513:8 | Scan code, identify food | CameraX exists; food lookup source uncertain | existing scanner UI, lookup unverified | pending |
| Coach recommendations | 2513:11 | Browse coaches and open profile | No coach backend; no individual photo assets | planned local catalogue | pending |
| Device connection | 2513:12–14 | Select device and pair/sync | Vendor SDKs, permissions, device availability | blocked for real sync | unavailable |
| Height entry | 2513:18 | Select and persist height | Profile update method required | planned | pending |
| Exercise tracking | 2513:39, 2513:50–52 | Start, time and complete session | Existing exercise log, session state needed | existing log; Figma details inaccessible | pending |
| Insights/progress/track | 2513:20, 2513:23–26, 2513:42, 2513:57 | Present activity statistics | Existing Room data; exact charts inaccessible | partial existing | pending |
| Notifications, period, sleep, subscription | 2513:27, 2513:38, 2513:40, 2513:46, 2513:53–55 | As depicted by inaccessible screens | Requirements and/or backend unknown | blocked | unavailable |

Remaining frame variants and names are recorded in `figma-screen-inventory.md`. A visual state alone is not counted as functional implementation.
