# Food API integration

## Provider and configuration

The app uses read-only Open Food Facts production endpoints. No API key or secret is required. Requests identify themselves with `KcalFit/1.0` and use the documented full-text search endpoint for name searches and v2 product lookup for barcodes.

Open Food Facts asks API users to identify their use through its API usage form and requires attribution under its ODbL/database-content terms. Product images are not used by this integration, avoiding separate image licensing concerns.

## Architecture

`FoodSearchScreen` keeps its existing design and add-to-meal callback. `CalFitViewModel` debounces and cancels query changes using `debounce`, `distinctUntilChanged`, and `flatMapLatest`. `FoodRepository` fetches live results through `OpenFoodFactsClient`, writes valid results into the existing Room `master_foods` cache, and emits cached results when the network is unavailable. Diary entries still use the existing `FoodEntryDao`, so Home, Nutrition, Track, Daily Goals, and chatbot totals remain on the same source of truth.

## Nutrition rules

Search results are normalized to a 100 g basis. Calories are read from `energy-kcal_100g`, falling back to `energy_100g / 4.184` only when kcal is absent. Protein, carbohydrate, fat, fiber, sugar, and sodium are read from their `_100g` values when available. A selected quantity is therefore scaled as `quantity / 100 * valuePer100g`; no ml-to-g conversion is made. Missing nutrients remain unavailable and are not invented as meaningful zeroes.

## Limits, caching, and offline behavior

Search is limited to 20 results, debounced by 350 ms, and cached in Room. Open Food Facts documents a 10-search-requests/minute/IP limit, so the app does not issue requests for blank queries and cancels obsolete requests. Previously cached results and saved diary entries remain readable offline. The provider may return incomplete or inaccurate community-contributed data; users can use manual entry when no trustworthy match exists.

## Verification

Automated verification: `./gradlew assembleDebug testDebugUnitTest`.

Live API verification requires a device/emulator with network access. The provider does not require a credential, so no further secret configuration is needed.
