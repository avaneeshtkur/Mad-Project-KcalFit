# Food nutrition API research

Research date: 2026-10-17.

## Decision

KcalFit uses Open Food Facts as its live read-only food search provider. It is the simplest safe first integration: reads require no API key, it has product/barcode data from around the world including India, and its product schema includes calories, macros, fiber, serving metadata, and both per-100-g and per-serving values. The app keeps selected results in the existing Room master-food cache, so saved diary entries remain available offline.

## Comparison

| Provider | Cost / access | Search and data | Coverage / limitations | Terms and suitability |
|---|---|---|---|---|
| [USDA FoodData Central API](https://fdc.nal.usda.gov/api-guide/) | Free public API, but every request needs a data.gov API key. Default limit is 1,000 requests/hour/IP; `DEMO_KEY` is only 30/hour and 50/day. | `/foods/search` and `/food/{fdcId}`. Strong nutrient data, generic/raw/foundation/FNDDS and branded data; serving/portion semantics vary by dataset. | Excellent generic and US branded coverage; Indian/local branded coverage is less dependable than Open Food Facts. A key must not be shipped in an Android APK, so a backend proxy would be required for production. | Data is public-domain CC0; USDA requests attribution. Best future server-side enrichment provider, not the direct mobile provider chosen here. |
| [Open Food Facts API](https://openfoodfacts.github.io/openfoodfacts-server/api/) | Free/open read API; read requests need no account or key, but require an identifying User-Agent. Limits: 10 search requests/min/IP and 15 product reads/min/IP. | Barcode lookup is supported by v2/v3. Current v2/v3 server search is structured; legacy `/cgi/search.pl` is the documented full-text search path. Nutrition includes calories, protein, carbs, fat, fiber, sugar, sodium, serving size and per-100-g/per-serving fields when supplied. | Very good branded/package and international/Indian product coverage; generic cooked/raw foods and data completeness vary because entries are community contributed. Search-as-you-type must be debounced and cached or it can be blocked. | Database ODbL, individual contents Database Contents License, images CC BY-SA. Attribution and terms review are required. No secret in APK; good direct mobile fit with transparent missing-data handling. |
| [Edamam Nutrition API](https://developer.edamam.com/edamam-nutrition-api) | Has a limited developer/free plan, but requires an application ID and application key; commercial quotas/terms vary by plan. | Ingredient parsing and nutrition lookup are strong, with quantity/unit handling. | Useful for ingredient/generic foods, but key protection and quota/terms make a direct APK integration inappropriate without a proxy. | Suitable only after confirming current plan terms and adding a protected backend; not selected for this implementation. |

## Implementation trade-off

Open Food Facts is not a laboratory-grade authority: values can be incomplete or user-entered. The app therefore preserves unknown macros as unavailable (`nutritionKnown = false`) rather than displaying fabricated zeroes, uses source nutrition per 100 g for quantity scaling, and retains the source product barcode in the cached catalog. Manual entry remains the fallback for foods without a reliable match.

Sources: [USDA API guide](https://fdc.nal.usda.gov/api-guide/), [Open Food Facts API documentation](https://openfoodfacts.github.io/openfoodfacts-server/api/), [Open Food Facts search documentation](https://openfoodfacts.github.io/documentation/docs/Product-Opener/v2/search/get-search/), [Open Food Facts licensing](https://openfoodfacts.github.io/documentation/docs/Product-Opener/api/tutorials/license-be-on-the-legal-side/), [Edamam Nutrition API](https://developer.edamam.com/edamam-nutrition-api).
