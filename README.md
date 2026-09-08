# Cal Fit (KcalFit)

A lightweight Android app for daily calorie and nutrition tracking — built as a simpler, no-frills alternative to apps like MyFitnessPal, without barcode or image scanning.

## Overview

Cal Fit makes everyday calorie tracking simple, quick, and easy to understand for users who want to monitor their food intake without the complexity of advanced diet apps. Users log the food they eat, record its calorie value, and get an organized daily nutrition summary — turning individual food entries into a clear picture of daily intake so they can make more informed dietary decisions.

The first version deliberately skips image recognition and barcode scanning to keep the app lightweight and focused on core tracking, with room to extend later.

## Features

- **Food & Calorie Entry** — Log foods consumed during the day and enter or select their calorie values, building a consistent daily food log.
- **Daily Nutrition Summary** — Calculates total calories for the selected day and presents them as a simple daily summary/chart, so users can compare intake against their personal target at a glance.
- **Daily History & Progress** — Stores past entries so users can review intake over time and spot changes in eating patterns instead of relying on memory.
- **Simple Personal Tracking** — A single, focused workflow for day-to-day calorie tracking with no advanced scanning or diet-planning overhead.

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | **Kotlin** | Primary language for the Android app and calorie-tracking logic |
| IDE | **Android Studio** | Build, test, debug, and package the app |
| UI | **Jetpack Compose** | App screens, input forms, the daily nutrition chart, and interactive UI |
| Persistence | **Room Database** | Local storage for food entries, calorie values, and daily nutrition records |
| Platform | **Android SDK & Material 3** | Device compatibility, navigation, layouts, and consistent Material Design UI |
| Data | **Local food-calorie dataset** | v1 uses a structured local dataset instead of image recognition or barcode-scanning APIs; external nutrition APIs are a candidate for later versions |

## Target Audience

Students, young adults, fitness enthusiasts, and anyone working toward weight-management goals who wants a fast, convenient way to log meals, understand daily energy intake, and review eating patterns — without navigating a feature-heavy diet app.

## Benefits

- Gives users a clearer picture of daily calorie consumption and builds awareness of eating habits.
- Encourages consistent self-monitoring by keeping food logging quick and simple.
- Surfaces patterns in daily intake through organized, historical records.
- Supports more informed food and portion decisions with easy-to-read calorie data.
- Offers a lightweight alternative for people who don't need advanced scanning or full diet-planning features.

## Getting Started

1. Clone the repository:
```bash
   git clone <repo-url>
```
2. Open the project in **Android Studio** (latest stable release recommended).
3. Let Gradle sync and download dependencies.
4. Run the app on an emulator or a physical Android device.

> Minimum SDK, target SDK, and package name are set in the module's `build.gradle` — check there for exact version requirements.

## Project Structure

Standard Android/Jetpack Compose project layout — UI screens (Compose), a Room database layer for local persistence (entries, calorie records), and the calorie/nutrition calculation logic sit alongside the usual Gradle build files. Update this section with the actual package layout as the codebase develops.

## Future Scope

1. **Barcode Scanning** — Recognize packaged foods to speed up nutrition entry.
2. **Food Image Recognition** — Estimate food items from photos to reduce manual entry for common meals.
3. **Personalized Calorie Goals** — Calculate daily targets from age, height, weight, activity level, and goals.
4. **Cloud Synchronization** — Sync food history across devices via secure cloud storage.
5. **Expanded Nutrition Tracking** — Track protein, carbs, fats, fiber, and other micronutrients beyond calories.

## Contributors

| Roll No. | Name |
|---|---|
| C032 | Avaneesh Thakur |
| C013 | Rishi Moradia |

B.Tech Integrated, Computer Engineering / Mobile Application & Development.

## References

Research referenced for the rationale and design of mobile dietary self-monitoring and calorie-tracking apps:

- Mateo, G. F., et al. (2022). *Nutrition-Related Mobile Application for Daily Dietary Self-Monitoring.* Journal of Healthcare Engineering. — https://doi.org/10.1155/2022/2476367
- Wang, Y., et al. (2022). *Sustainability of Weight Loss Through Smartphone Apps: Systematic Review and Meta-analysis.* JMIR mHealth and uHealth. — https://pmc.ncbi.nlm.nih.gov/articles/PMC9536524/
- Flores Mateo, G., et al. (2020). *Effect of Behavioral Weight Management Interventions Using Lifestyle mHealth Self-Monitoring on Weight Loss.* — https://pmc.ncbi.nlm.nih.gov/articles/PMC7400167/
- Burke, L. E., et al. (2011). *Self-Monitoring in Weight Loss: A Systematic Review of the Literature.* Journal of the American Dietetic Association. — https://pmc.ncbi.nlm.nih.gov/articles/PMC3268700/
- Villinger, K., et al. (2019). *A Focused Review of Smartphone Diet-Tracking Apps.* JMIR mHealth and uHealth. — https://mhealth.jmir.org/2019/5/e9232
- Payne, J. E., et al. (2018). *Defining Adherence to Dietary Self-Monitoring Using a Mobile App: A Narrative Review.* Journal of the Academy of Nutrition and Dietetics. — https://pubmed.ncbi.nlm.nih.gov/30115555/
- Evans, E. H., et al. (2021). *A Systematic Review and Meta-Analysis of Validation Studies Performed on Dietary Record Apps.* Advances in Nutrition. — https://pubmed.ncbi.nlm.nih.gov/34019624/
