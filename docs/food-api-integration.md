# Food search integration

## Runtime path

`AddFoodScreen` → `FoodSearchEngine` → `FoodSearchRepository` → a normalized in-memory index of Room `master_foods` → optional Open Food Facts refresh. Local lookup runs on `Dispatchers.Default` immediately; only refresh waits for a 300 ms pause. Engine jobs are cancelled on each normalized query change and when Add Food is disposed. A generation guard rejects obsolete responses. Identical normalized input does not reset results or issue another request.

The UI distinguishes Idle, Loading, Refreshing with matches, Results, confirmed Empty, and Error with retained matches and manual retry. Selecting either cached or refreshed food uses the same `FoodDiaryRules.fromCatalog` conversion and existing food-entry persistence. Diary entries are independent nutrition snapshots: cache pruning does not delete or modify them.

## Relevance and bounds

Names are normalized with Unicode NFKD, diacritic removal, Locale.ROOT case folding, punctuation separation, and whitespace collapse. Ranking uses exact name, whole-name prefix, word-boundary prefix, all-token prefixes, substring, and edit-distance-one fuzzy matches. Brand-only matches follow name matches. Chapati/chapathi/chappati and panir have explicit spelling aliases; the typed exact name still wins. Stable tie breaks use name, brand, barcode, and local ID. Deduplication uses barcode for provider products and a name/brand/portion/nutrition signature for other foods, retaining different preparations.

The index pre-normalizes at most 10,000 Room rows (local/saved rows first). Open Food Facts returns at most 60 candidates per refresh; the UI displays at most 50 ranked matches. Client ranking cannot retrieve a product missing from that bounded provider candidate set or invent foods absent from the local catalog.

## Persistent cache

Room v7 migrates v6 without resetting the database or changing diary rows. It adds source/fetch metadata and a query-freshness table. Existing remote duplicates are consolidated, retaining stable IDs and favorites. Refreshes update existing provider products by barcode instead of appending an auto-generated row every time.

Cache keys are canonical normalized queries. A successful refresh, including a valid zero-result response, stays fresh for 24 hours. At most 100 recent query keys and 2,000 non-favorite remote products are retained. Remote cache rows expire after 30 days. User-created/local rows and favorites are not discarded by cache pruning. Eviction clears freshness keys so an evicted result is never mistaken for a fresh confirmed absence. Failures do not update freshness or remove existing foods. Custom inserts and asset imports invalidate the in-memory snapshot.

Source provenance remains attached to individual rows; local/manual data is not added to or used to overwrite an OFF product's nutrient values. The UI attributes OFF results. Do not publish a combined external catalog as OFF data or redistribute a derived OFF database without satisfying the ODbL requirements.

## Network reliability

The existing production full-text endpoint is `/cgi/search.pl`; v2 structured search does not support free-text queries. Reads need no API key. Search refresh attempts share one gate with a minimum 6.5 seconds between requests, including retries/manual attempts. Barcode reads have a separate 4.1 second gate. This keeps local typing independent of the API and bounds remote enrichment below OFF's documented per-device search/read ceilings. IP-wide limits can still apply when devices share a network. OFF advises against using its public endpoint directly for typeahead; high-traffic deployments should provide a licensed local catalog or their own backend/export index instead of reducing these intervals.

A request has a 3 second connection timeout, 5 second read timeout, 6 second overall attempt limit, and 24 second operation budget including waits. Socket cancellation disconnects active connections. There is one retry layer: at most two retries for connectivity errors, 429, 500, 502, 503, and 504, with exponential delay and jitter. Retry-After seconds and HTTP dates establish a shared cooldown; requests never bypass it. Other 4xx responses and invalid JSON/schema responses are not automatically retried. When recovery is exhausted, a manual retry is offered while saved matches remain visible. Provider downtime/global throttling can still return 503.

Debug-only `FoodSearch` logs contain event, duration, candidate count, status, and retry index. They do not contain query text, food names, tokens, or user IDs. Events distinguish local lookups, fresh-cache hits, HTTP attempts, and cancellation.

## Configuration and data

Add `OPEN_FOOD_FACTS_CONTACT` to ignored `local.properties`, using the actual maintainer contact email or contact URL, then rebuild. It identifies the app in its User-Agent. No food API key or service-role credential is needed. Complete OFF's API usage form before release.

There is currently no `app/src/main/assets/foods.csv` bundled in this checkout. Existing device catalog rows are reused. The optional importer is now invoked during application startup if a licensed dataset is supplied at that path. It accepts `food_name,calories,protein,carbs,fat` and optional `fiber,sugar,sodium_mg,serving_size,serving_unit`, up to 5,000 rows. Specify the real serving basis. No fabricated starter nutrition is inserted. A fresh installation can only show known/imported/cached foods immediately; an unknown food still depends on the provider or manual entry.

The provider mapper requires usable calories, name, and stable product code. Per-100-g nutrient fields are used directly. A per-serving fallback is converted only when its gram weight is explicit; no ml-to-g inference occurs. The legacy non-null numeric columns use internal zero placeholders for unavailable values, but an incomplete macro set is explicitly marked `nutritionKnown=false`. Optional micronutrient absence is not claimed as a measured zero.

## Licensing and references

OFF supports local caching under ODbL / Database Contents License; no product images are fetched. The 24-hour freshness and 30-day retention periods are Calfit's resource/refresh policy, not an invented provider retention mandate.

- [OFF rate limits, 503 behavior, User-Agent and full-text search](https://openfoodfacts.github.io/openfoodfacts-server/api/)
- [OFF local caching guidance](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorials/creating-a-local-cache-of-open-food-facts-data/)
- [OFF licensing and attribution](https://openfoodfacts.github.io/documentation/docs/Product-Opener/api/tutorials/license-be-on-the-legal-side/)

## Verification

`./gradlew testDebugUnitTest assembleDebug lintDebug`

Unit tests use virtual coroutine time, a fake store, and mocked HTTP responses. They never call a live provider. The ranking benchmark prints `FOOD_BENCH` with measured host JVM index-build and warm-search timings; these are not Android frame-time claims. Live end-to-end UI validation requires a connected device/emulator and a populated local catalog.

## Files changed for the resilient-search update

All paths below are relative to `app/src/main/java/com/kcalfit/app/` unless stated otherwise. Existing unrelated working-tree edits were preserved; authentication, navigation, and branding were not edited in this update.

| Files | Purpose |
| --- | --- |
| `data/search/FoodSearchRepository.kt`, `RoomFoodSearchStore.kt` | Cache-first search, freshness checks, deduplicated merges, Room snapshot invalidation. |
| `data/search/FoodSearchRanking.kt` | Normalization, aliases, prefix/token/fuzzy tiers, stable identity and ranking. |
| `data/search/AndroidFoodSearchDiagnostics.kt` | Redacted debug timing, status, cache, and retry events. |
| `data/remote/FoodRequestPolicy.kt`, `FoodHttpTransport.kt`, `OpenFoodFactsClient.kt` | Shared rate gates, bounded retries, Retry-After, socket cancellation, timeout and response validation, provider mapping. |
| `data/local/FoodItemDao.kt`, `AppDatabase.kt` | Transactional barcode upserts, bounds/eviction, catalog invalidation, non-destructive v6-to-v7 migration. |
| `data/model/FoodItemEntity.kt`, `FoodSearchCacheEntity.kt` | Source/fetch metadata and normalized-query freshness records. |
| `ui/viewmodel/FoodSearchEngine.kt`, `CalFitViewModel.kt` | Immediate local lookup, debounced refresh, duplicate suppression, cancellation/generation guards, explicit UI states. |
| `ui/screens/AddFoodScreen.kt` | Keep matches during refresh/failure, selection and dispose cancellation, provider attribution. |
| `domain/calculator/FoodDiaryRules.kt` | Shared, tested catalog-to-diary portion conversion. |
| `CalFitApplication.kt` | Activate the optional existing licensed CSV importer off the main thread. |
| `app/build.gradle.kts` | Contact User-Agent setting; test-only coroutine dispatcher and JSON dependencies. No production dependency added. |
| `app/src/test/.../FoodSearchEngineTest.kt`, `FoodSearchRankingTest.kt`, `FoodSearchRepositoryTest.kt`, `FoodRequestPolicyTest.kt`, `FoodHttpTransportTest.kt` | Deterministic state, ranking, cache, mock HTTP, retry and portion-calculation checks. |
| `docs/food-api-integration.md` | Policies, configuration, provenance, limitations, and validation evidence. |

## Observed verification, 17 October 2026

- Unit tests and debug APK build passed. The full suite contains 55 tests, with zero failures at the final code review run.
- A 5,000-candidate host JVM benchmark after warm-up measured 1.271 ms median and 1.565 ms p95 search latency. Cold index construction was 32.011 ms and runs off the UI thread. These are synthetic host measurements, not Android UI frame measurements; test XML contains the exact run output.
- A deterministic workload of ten normalized repeated refresh calls made one remote call (nine reused cache). A four-step rapid typing burst made one remote request after the debounce. These are controlled test workloads, not a claimed production cache-hit rate.
- SQLite execution of the actual v6-to-v7 migration SQL preserved a sample diary, local/custom foods, and favorites while removing a duplicate remote row. Full Room migration instrumentation still requires an Android runtime.
- One timed production endpoint probe for `RIC` returned HTTP 503 in 0.668161 seconds. No healthy remote latency or successful current end-to-end fetch is claimed from this probe.
- `lintDebug` and its stack-trace rerun crashed before findings: `IllegalArgumentException: 25.0.2` from `com.intellij.util.lang.JavaVersion.parse`, in the lint/Kotlin analysis runtime. The checkout's daemon criteria select Java 25. The unrelated toolchain configuration was left unchanged.
- ADB reported no attached device/emulator. Android rendering and on-device Room migration remain unverified.
