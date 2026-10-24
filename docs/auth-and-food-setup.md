# CalFit authentication and food catalog setup

The app uses Supabase Auth's REST API for email/password accounts and accepts Supabase's six-digit confirmation token in the app. It does not use a Supabase Kotlin SDK.

Supabase references: [redirect URLs](https://supabase.com/docs/guides/auth/redirect-urls), [native mobile deep linking](https://supabase.com/docs/guides/auth/native-mobile-deep-linking), [password recovery](https://supabase.com/docs/guides/auth/passwords), and [Google provider setup](https://supabase.com/docs/guides/auth/social-login/auth-google).

1. Create a Supabase project. In **Authentication → Providers → Email**, enable email signups and **Confirm email**. Keep phone confirmation off if you want a clear duplicate-email error for an already registered address.
2. In **Authentication → Email Templates → Confirm signup**, include a confirmation link using `{{ .ConfirmationURL }}` and the six-digit `{{ .Token }}` as a manual fallback. The link confirms the address and opens the Android app; entering the code continues to use the app's existing verification screen.
3. In **Authentication → URL Configuration**, set **Site URL** to `com.kcalfit.app://auth/callback` for this native-only app, and add the exact same value under **Redirect URLs**. The app supplies it for signup and password recovery. If CalFit gets a real website later, use its HTTPS URL for Site URL and keep this app URL in Redirect URLs. Do not leave localhost as the fallback. Preserve `{{ .ConfirmationURL }}` in the recovery template so the recovery request is verified before redirecting to the app. Configure a production SMTP provider before release; Supabase's default mail service has delivery and rate limits.
4. Google sign-in is native: Android Credential Manager returns a Google ID token, which the app exchanges at Supabase's `token?grant_type=id_token` endpoint with a one-time nonce (Google receives its SHA-256; Supabase receives the raw value). No browser redirect is used.
   - **Google Cloud → Credentials → Web application** OAuth client: its client ID is `GOOGLE_WEB_CLIENT_ID` below. Add `https://YOUR_PROJECT.supabase.co/auth/v1/callback` as an authorized redirect URI.
   - **Google Cloud → Credentials → Android** OAuth client: package `com.kcalfit.app` and the SHA-1 of every signing key — each developer's debug key (`./gradlew signingReport`) and the release or Play App Signing key. Without a matching SHA-1 Credential Manager reports that no credentials are available.
   - **Google Cloud → OAuth consent screen**: publish it, or add each tester under Test users.
   - **Supabase → Authentication → Providers → Google**: enabled, with the Web client ID and secret; add the Web client ID to **Client IDs** (authorized client IDs for ID tokens). Leave **Skip nonce check** off.
   - Never put the Google client secret in the Android app.
5. Email/password registration supports both the six-digit confirmation screen and confirmation-link deep linking. Keep both `{{ .ConfirmationURL }}` and `{{ .Token }}` in **Confirm signup**. The app sends `redirect_to=com.kcalfit.app://auth/callback` with signup; after the user taps the confirmation link, Supabase verifies it and returns session tokens to Android.
6. Add these *build-time* settings to the project's ignored `local.properties` and rebuild:

   ```properties
   SUPABASE_URL=https://YOUR_PROJECT.supabase.co
   SUPABASE_PUBLISHABLE_KEY=YOUR_PUBLISHABLE_OR_ANON_KEY
   GOOGLE_WEB_CLIENT_ID=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com
   ```

   Use only the publishable/anon key in the Android app, never a service-role key. With empty placeholders, registration shows a configuration error; it never pretends to send an email.

The current update migrates version 5 to 6 without deleting diary entries. It adds nutrition-availability metadata and removes synthetic starter barcodes. Existing all-zero macros are marked unavailable because their original source cannot establish whether they were recorded. Explicit new zero values remain valid. Older database versions require explicit migrations; destructive fallback is disabled. Supabase stores accounts and onboarding profile metadata; logs remain local to this device. The existing single-account model clears local data when switching accounts.

For a larger catalog, place a UTF-8 `foods.csv` at `app/src/main/assets/foods.csv` with `food_name,calories,protein,carbs,fat`; optional `fiber,sugar,sodium_mg,serving_size,serving_unit` are supported. Include the actual nutrition basis so quantity scaling is meaningful. Missing macro columns are marked unavailable. Up to 5,000 rows plus built-in estimate foods are supported; no larger dataset is currently bundled. Manual logging lets the user enter packaging values for their entire portion.

Login supports Android Credential Manager: Use Saved Password retrieves from the configured device provider; Save with password manager offers saving after Supabase succeeds. Passwords are not stored by KcalFit. Refresh tokens are encrypted with AES-GCM using Android Keystore and stored outside backup, with migration from the former preferences token. Sign-out clears app/provider state and attempts Supabase session revocation. Provider prompts and live authentication must be checked on the target device.
