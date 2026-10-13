# Nakafit 54 reference screen audit

This audit accounts for every mockup exposed by the Nakafit Figma page (`🖼 Cover`, page node `0:1`). The original node mapping and access limits are recorded in [figma-screen-inventory.md](figma-screen-inventory.md). The implementation uses distinct Compose routes where a reference represents a distinct destination and local state for flattened variants.

| Node | Reference | Compose destination/state | Status |
|---|---|---|---|
|2513:7|Add Goals|`add_goals`|implemented|
|2513:8|Barcode Scan|`barcode_lookup`|implemented (local lookup; camera integration pending)|
|2513:9|Breakfast - Daily|`breakfast` daily state|implemented|
|2513:10|Breakfast|`breakfast`|implemented|
|2513:11|Coach Recommendation|`coaches`|implemented|
|2513:12|Complete|device connection state|implemented|
|2513:13|Connect Apple Watch|device connection state|implemented|
|2513:14|Connection Device|device connection state|implemented|
|2513:15|Daily Goals - Morning Run|`daily_goal/{title}/{activity}`|implemented|
|2513:16|Daily Health Log Entry|`health_log`|implemented|
|2513:17|Detail Menu|`meal_detail/{id}`|implemented|
|2513:18|Entry Height|progress measurement state|implemented|
|2513:19|Entry Weight|progress measurement state|implemented|
|2513:20|Health Metrics Summary|`health_summary`|implemented|
|2513:21|Home Screen - Scroll|home scroll state|implemented by responsive scroll|
|2513:22|Home Screen|`home`|implemented|
|2513:23|Insight - All 1|`insight` selected state|implemented|
|2513:24|Insight - All 2|`insight` selected state|implemented|
|2513:25|Insight - Daily Goals|`insight` goals state|implemented|
|2513:26|Insight|`insight`|implemented|
|2513:27|Manage Notifications|`notifications`|implemented|
|2513:28|Meal Scan|`meal_scan`|implemented as local preview|
|2513:29|Nutritions Menu - Scroll|nutrition scroll state|implemented by responsive scroll|
|2513:30|Nutritions Menu|`nutrition`|implemented|
|2513:31|Onboarding - Success Set Goals|onboarding completion state|implemented|
|2513:32|Onboarding-1|onboarding step 1|implemented|
|2513:33|Onboarding-2|onboarding step 2|implemented|
|2513:34|Onboarding-3|onboarding step 3|implemented|
|2513:35|Onboarding-4|onboarding step 4|implemented|
|2513:36|Onboarding-5|onboarding step 5|implemented|
|2513:37|Onboarding|`onboarding`|implemented|
|2513:38|Period|`period`|implemented|
|2513:39|Pop Up - Complete Exercise|workout session completion state|implemented|
|2513:40|Premium Features|`premium`|implemented|
|2513:41|Profile Coach|`coach_profile/{id}`|implemented|
|2513:42|Progress|`progress`|implemented|
|2513:43|Search Menu|`search` filter state|implemented|
|2513:44|Search|`search`|implemented|
|2513:45|Set Up Apple Watch|device connection state|implemented|
|2513:46|Sleep Quality|`sleep`|implemented|
|2513:47|Splash Screen-1|`splash` loading state|implemented|
|2513:48|Splash Screen-2|`splash` loading state|implemented|
|2513:49|Splash Screen|`splash`|implemented|
|2513:50|Start Exercise - Time Moving|`workout_session/{id}` active state|implemented|
|2513:51|Start Exercise 1|`workout_session/{id}` initial state|implemented|
|2513:52|Start Exercise 2|`workout_session/{id}` paused state|implemented|
|2513:53|Subscription Plan - Select|`subscription` selected state|implemented|
|2513:54|Subscription Plan - Set|`subscription` confirmation state|implemented|
|2513:55|Subscription Plan|`subscription`|implemented|
|2513:56|Suggestions Menu|`suggestions`|implemented|
|2513:57|Track|`track`|implemented|
|2513:58|User Profile Information|`profile_info`|implemented|
|2513:59|Your Meal - Detail Nutririon-1|`meal_detail/{id}` state|implemented|
|2513:60|Your Meal - Detail Nutririon|`meal_detail/{id}`|implemented|

## Verification boundary

The project has been checked with `./gradlew assembleDebug`. Per the user's instruction, no emulator or screenshot run was performed in this pass. The 43 Figma PNGs unavailable through the MCP limit remain documented as approximated states rather than falsely claimed visual comparisons.
