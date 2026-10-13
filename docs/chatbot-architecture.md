# Chatbot architecture

Audit: the old chatbot used broad substring tests (including “today” and “eat”) before specific intents. It had no conversation context, navigation actions, catalogue retrieval, or LLM integration. Responses included ungrounded food estimates. Chat history is stored in Room and scoped by the existing account lifecycle.

Implementation: structured intent/entity recognition → bounded conversation context → Room diary/catalogue snapshot → deterministic grounded reply → allowlisted navigation. No food mutation from chat; additions open the existing review form and deletion opens the diary. A response-service interface supports a future authenticated server model, with timeout/error fallback. No AI endpoint or provider secret is configured; the local assistant is not an LLM. `FitnessChatbotTest` covers 21 cases including variant phrasing, records, clarifications, safe actions, context expiry, and remote timeout fallback.

Personal questions default to the device's current date; explicit yesterday/ISO date and follow-up dates are resolved separately from the app's historical selection. Missing macros remain disclosed. Catalogue nutrition is labeled as an estimate and only scales compatible units.

## Completed behavior

- Structured CoachRequest/CoachIntent and CoachAction models replace the old generic dispatcher. Specific macro, meal, food, technique and navigation intent ordering avoids the original broad-keyword collision.
- Personal totals share FoodDiaryRules.totals with Home's ViewModel; direct Room snapshots prevent reliance on a stale historical screen. Four meal filters and whole-day reviews, today/yesterday/ISO/selected dates, partial macro disclosure, distinct weekly activity days and daily goals use saved records.
- Food context carries quantity/unit into follow-ups. It expires after 15 minutes or 12 turns and resets on unrelated topics/account changes. Raw rice is never substituted with steamed rice. Unknown unit conversions ask for clarification.
- Navigation uses an enum allowlist and an explicit Open action. Named catalogue additions open the existing form for review. No chat path inserts or deletes food. The unused older processVoiceInput method that silently inserted estimated foods was removed.
- Local exercise guidance is intentionally limited to beginner sessions and supported techniques, not a reasoning model. Exercise safety references reviewed: [NHS strength exercises](https://www.nhs.uk/live-well/exercise/strength-exercises/) and [Mayo Clinic core guidance](https://www.mayoclinic.org/healthy-lifestyle/fitness/in-depth/core-strength/art-20546851).

## Remaining AI integration

No usable LLM client, authenticated AI endpoint, provider configuration or server function exists in this project. CoachResponseService is the injectable suspend interface; CoachResponder provides an eight-second timeout and error fallback. It currently has no remote implementation and sends no data externally. To enable a model: deploy an authenticated server endpoint (for example a Supabase Edge Function), keep provider credentials in that server's secret store, enforce fitness scope and safe guidance there, then implement/inject CoachResponseService using a valid user token. Test provider failures and server authorization before enabling it. Do not use the publishable Supabase key as a model credential.

Personal calculations and actions stay deterministic. Only general workout/technique questions are eligible for the optional service; a model reply cannot produce a navigation action or database mutation. The current catalogue contains legacy estimates with generic servings; requests such as 250 g of rice cannot receive a trustworthy number until a matching gram-based record or reliable source is supplied. This is reported in chat instead of guessing.
