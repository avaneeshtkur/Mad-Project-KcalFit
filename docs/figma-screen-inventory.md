# Nakafit screen inventory

Source: [Nakafit Figma file](https://www.figma.com/design/yfy2viol77Ap3I62co7lgV/Nakafit---Health-and-Fitness-Tracker-Mobile-App-UI-Kit----Free-landing-page--Preview-?node-id=0-1). Inspected 2026-10-14.

The accessible file contains one page, `🖼 Cover` (`0:1`). Its 54 mobile mockups are each a single 375 × 812 rounded rectangle with a flattened PNG fill. The MCP metadata supplies names and node IDs, but no component tree, typography properties, interactive prototypes, or separate photo/icon assets. Context and visual references were obtained for the ten rows marked **seen** below. The Figma Starter plan call limit prevented retrieving the other 44 PNGs. Names alone do not establish exact screen content or behavior.

| Node | Frame name | Access | Intended area / likely navigation |
| --- | --- | --- | --- |
| 2513:7 | Add Goals | seen | Goal creation form; likely Track/Goals |
| 2513:8 | Barcode Scan | seen | Barcode capture from nutrition flow |
| 2513:9 | Breakfast - Daily | seen | Day-specific breakfast intake and macros |
| 2513:10 | Breakfast | seen | Breakfast date list and meal items |
| 2513:11 | Coach Recommendation | seen | Coach directory |
| 2513:12 | Complete | seen | Device setup success dialog variant |
| 2513:13 | Connect Apple Watch | seen | Watch connection intro dialog variant |
| 2513:14 | Connection Device | seen | Device brand selector dialog variant |
| 2513:15 | Daily Goals - Morning Run | inaccessible | Goal detail |
| 2513:16 | Daily Health Log Entry | inaccessible | Health log entry |
| 2513:17 | Detail Menu | seen | Meal nutrition detail |
| 2513:18 | Entry Height | seen | Height entry dialog over Progress |
| 2513:19 | Entry Weight | inaccessible | Weight entry dialog |
| 2513:20 | Health Metrics Summary | inaccessible | Health summary |
| 2513:21 | Home Screen - Scroll | inaccessible | Home scrolled state |
| 2513:22 | Home Screen | seen | Home dashboard |
| 2513:23 | Insight - All 1 | inaccessible | Insight list state |
| 2513:24 | Insight - All 2 | inaccessible | Insight list state |
| 2513:25 | Insight - Daily Goals | inaccessible | Goal insights |
| 2513:26 | Insight | inaccessible | Insight overview |
| 2513:27 | Manage Notifications | inaccessible | Notification preferences |
| 2513:28 | Meal Scan | inaccessible | Camera meal scan |
| 2513:29 | Nutritions Menu - Scroll | inaccessible | Nutrition scrolled state |
| 2513:30 | Nutritions Menu | inaccessible | Nutrition overview |
| 2513:31 | Onboarding - Success Set Goals | inaccessible | Onboarding completion |
| 2513:32 | Onboarding-1 | inaccessible | Onboarding step |
| 2513:33 | Onboarding-2 | inaccessible | Onboarding step |
| 2513:34 | Onboarding-3 | inaccessible | Onboarding step |
| 2513:35 | Onboarding-4 | inaccessible | Onboarding step |
| 2513:36 | Onboarding-5 | inaccessible | Onboarding step |
| 2513:37 | Onboarding | inaccessible | Onboarding entry |
| 2513:38 | Period | inaccessible | Period tracking |
| 2513:39 | Pop Up - Complete Exercise | inaccessible | Exercise completion state |
| 2513:40 | Premium Features | inaccessible | Subscription benefits |
| 2513:41 | Profile Coach | inaccessible | Coach profile |
| 2513:42 | Progress | inaccessible | Progress dashboard |
| 2513:43 | Search Menu | inaccessible | Search category state |
| 2513:44 | Search | inaccessible | Search screen |
| 2513:45 | Set Up Apple Watch | inaccessible | Watch setup |
| 2513:46 | Sleep Quality | inaccessible | Sleep view |
| 2513:47 | Splash Screen-1 | inaccessible | Splash variant |
| 2513:48 | Splash Screen-2 | inaccessible | Splash variant |
| 2513:49 | Splash Screen | inaccessible | Splash variant |
| 2513:50 | Start Exercise - Time Moving | inaccessible | Active session state |
| 2513:51 | Start Exercise 1 | inaccessible | Exercise session state |
| 2513:52 | Start Exercise 2 | inaccessible | Exercise session state |
| 2513:53 | Subscription Plan - Select | inaccessible | Plan selection state |
| 2513:54 | Subscription Plan - Set | inaccessible | Plan confirmation state |
| 2513:55 | Subscription Plan | inaccessible | Plan list |
| 2513:56 | Suggestions Menu | inaccessible | Suggestions |
| 2513:57 | Track | inaccessible | Tracking dashboard |
| 2513:58 | User Profile Information | inaccessible | Profile info |
| 2513:59 | Your Meal - Detail Nutririon-1 | inaccessible | Meal detail variant |
| 2513:60 | Your Meal - Detail Nutririon | inaccessible | Meal detail variant |

## Observed design and interactions

- **Home Screen (`2513:22`):** white canvas; greeting/avatar and notification action; black workout progress card with white/purple ring; two health overview metrics; purple and pale lime exercise cards with photographs; black pill bottom navigation with purple active state. Visible actions: notification, See Details, exercise Start Now, and five navigation tabs. The screenshot contains sample statistics, not live user values.
- **Add Goals (`2513:7`):** white rounded sheet over grey scrim; title input; 8 activity choices; Daily/Weekly/Monthly/Yearly selector; weekday chips; Every Days switch; reminder time; purple Add Goals button. A goal needs local persistence or an existing repository and form validation.
- **Breakfast (`2513:10`) / Breakfast - Daily (`2513:9`):** dated meal list; add buttons; daily calorie bar; macro rings; Scan a Meal and Scan Barcode buttons. Existing food entries/search can supply real data; the displayed food photography was flattened into the PNG and is not independently exportable.
- **Barcode Scan (`2513:8`):** camera preview, torch action, viewfinder and capture action. A nutrition lookup service is needed for actual barcode identification; existing project has a scanner UI, but data lookup must be checked.
- **Coach Recommendation (`2513:11`):** vertical coach cards with photo, name, age, summary, specialty, frequency. The file exposes no coach data API or separate portraits.
- **Detail Menu (`2513:17`):** hero food photo, kcal badge, four macro rings, ingredient list. Photography and ingredients are flattened into the screen PNG.
- **Connect Apple Watch (`2513:13`), Connection Device (`2513:14`), Complete (`2513:12`):** sequential dialog variants; the first offers Set Up Now and Skip, the next selects Apple Watch, Garmin, Fitbit, Polar, or Suunto, and Complete displays success. Android has no existing health-device SDK or permission flow, so actual synchronization cannot be inferred from this preview.
- **Entry Height (`2513:18`):** height picker sheet with 170 centimeters selected over a progress screen; Select writes a profile measurement if the profile model supports it.

## Asset and specification limits

Ten complete-screen PNGs are stored in `docs/figma-references/` for visual analysis only. They are not suitable implementation assets: using the screenshot as a UI would make controls and data static. No standalone photos, logo, SVG icons, fonts, or layer measurements were accessible. Exact colors and typography for the ten viewed screens are visual estimates until editable design data becomes available.
