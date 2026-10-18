# Manual service setup and verification

No migration, edge function, payment service, or production configuration was deployed during implementation. The app reports unavailable services honestly. Manual food logging is independent of scanning and billing.

## Build settings

- Compile SDK 36 and AGP 8.10.1 are required by Health Connect 1.1.0. Runtime target remains 35 and minimum remains 26.
- New dependencies: Health Connect 1.1.0, WorkManager 2.10.0, Play Billing 8.0.0, ExifInterface 1.3.7.
- Existing `SUPABASE_URL` / `SUPABASE_PUBLISHABLE_KEY` settings are reused. Add `PLAY_PREMIUM_PRODUCT_ID` to local.properties for the Play subscription's product identifier. This is a public product ID, not a secret.

## Food scanning and five free scans

1. Review `supabase/migrations/202610180001_scan_entitlements.sql` before applying it in your Supabase project. It creates isolated tables; it does not alter existing food/auth tables.
2. Deploy `scan-food`, `account-entitlement`, and `verify-play-purchase` with JWT verification enabled. Each function additionally verifies the bearer token with Supabase Auth. Never expose the service-role key in Android.
3. Configure server secrets `OPENAI_API_KEY` and `FOOD_VISION_MODEL` (an image-capable Chat Completions model supporting JSON output). The provider key exists only in server environment variables. Configure spending/rate limits on the provider account.
4. The function identifies foods/estimated grams; the Android client matches against the existing local/remote food catalogue. Users select a match, edit portions, review macros, and explicitly save through the existing Add Food validation. No provider-supplied nutrition is silently accepted. Unmatched foods remain manual entries.
5. A database row lock serializes reservations per account. Pending reservations occupy free quota, successful results increment it once, failure releases it, and a retry with the same request ID returns the successful cached result. Lease IDs prevent an expired worker from completing a newer retry. Client roles cannot invoke quota functions or write entitlement tables. Five **successful** free scans are counted, independent of how many foods a scan identifies.
6. Photos are JPEG-compressed locally with EXIF orientation handled, sent only after consent, and not stored by these edge functions. Camera cache files and temporary gallery access remain device-local. Review the vision provider's data-retention terms for your account before release.

## Premium and payments

Google Play Billing is the implemented purchase path. No Razorpay checkout is added to a Play-distributed digital subscription without applicable program enrollment.

1. Create an active subscription/base plan in Play Console, configure product ID in Android and server `PLAY_PREMIUM_PRODUCT_ID`, and distribute a signed test build via a Play testing track.
2. Set server `GOOGLE_PLAY_SERVICE_ACCOUNT` to the service-account JSON authorized for the Google Play Developer API. Keep it only in server secrets.
3. The app sets an obfuscated account ID derived from the authenticated user ID. The backend verifies the token with Google's subscriptions v2 API, validates the product, account association, state and expiry, and acknowledges a valid new purchase. A client callback never grants premium.
4. Restore queries active Play purchases and re-verifies them. Each entitlement check and premium scan revalidates any unexpired subscription against Google, so cached client flags cannot grant scans. Provider outages fail closed.
5. Validate purchase, cancellation, renewal, grace period, refund/revocation, pending payment, restore and account-switch cases with Play license testers. Live transactions were not tested.
6. Real-time Developer Notifications and scheduled entitlement reconciliation may be added for proactive server notifications; access checks already verify live status. Razorpay remains a separate distribution/enrollment decision. For eligible alternative billing, implement Google's required choice UX/reporting and Razorpay server signature/webhook verification before enabling checkout.

## Steps

- Health Connect reads aggregate step/distance totals from local midnight in the current time zone. It does not sum overlapping raw records. Sensor and Health Connect totals are never added together.
- Separate user permission enables Health Connect background reads; WorkManager refreshes periodically (minimum 15 minutes, subject to OS scheduling).
- The device counter fallback requires activity recognition and a supported TYPE_STEP_COUNTER sensor. A user-started health foreground service maintains counts while enabled. The first callback establishes a baseline; reboot and midnight-spanning batches are not assigned to the wrong day. Therefore fallback counts can be partial, unlike a provider's full-day aggregate.
- The service intentionally does not restart itself after reboot. Start it again from More → Automatic steps. Health Connect is preferred for complete daily history.
- Health rationale/permission-usage activity and manifest entries are included. Complete Play's Health Connect permissions/data-safety declarations and verify denied/revoked permissions on a physical supported device.

## Remaining configuration

- Published privacy-policy/terms URLs, Apple OAuth provider setup and outbound marketing/news delivery are not supplied. The UI explains their unavailable state. Consent/preferences are stored locally; no marketing subscription is fabricated.
- Account-created reminders currently store an opt-in preference; notification delivery requires additional setup and is identified as unavailable in the UI.
- Existing Supabase sign-in, password manager, live food network calls, camera, permission dialogs and billing need device/provider testing. No live accounts were created or payments made.

## Primary implementation references (checked 2026-10-18)

- [Health Connect reading and aggregation](https://developer.android.com/health-and-fitness/health-connect/read-data)
- [Health Connect setup and permission rationale](https://developer.android.com/health-and-fitness/health-connect/get-started)
- [Google Play Billing integration](https://developer.android.com/google/play/billing/integrate)
- [Google Play Payments policy](https://support.google.com/googleplay/android-developer/answer/9858738)
- [India alternative billing requirements](https://support.google.com/googleplay/android-developer/answer/13306652)
- [Razorpay Android prerequisites](https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/)
- [OpenAI image inputs](https://developers.openai.com/api/docs/guides/images-vision)
- [AGP API 36 support](https://developer.android.com/build/releases/agp-8-10-0-release-notes)
