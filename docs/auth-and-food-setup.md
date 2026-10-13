# CalFit authentication and food catalog setup

The app uses Supabase Auth for email/password accounts. No OTP is generated or accepted on-device.

1. Create a Supabase project. In **Authentication → Providers → Email**, enable email signups and **Confirm email**. Keep phone confirmation off if you want a clear duplicate-email error for an already registered address.
2. In **Authentication → Email Templates → Confirm signup**, put the six-digit `{{ .Token }}` in the message body (for example, “Welcome to CalFit. Your one-time password is {{ .Token }}”). The stock confirmation-link template does not match the app's six-digit entry screen.
3. Configure a production SMTP provider in Supabase before production use; the default mail service has delivery and rate limits. Also configure the password-recovery template and redirect destination if you plan to use reset links.
4. Add these *build-time* settings to the project's ignored `local.properties` and rebuild:

   ```properties
   SUPABASE_URL=https://YOUR_PROJECT.supabase.co
   SUPABASE_PUBLISHABLE_KEY=YOUR_PUBLISHABLE_OR_ANON_KEY
   ```

   Use only the publishable/anon key in the Android app, never a service-role key. With empty placeholders, registration shows a configuration error; it never pretends to send an email.

The current update migrates version 5 to 6 without deleting diary entries. It adds nutrition-availability metadata and removes synthetic starter barcodes. Existing all-zero macros are marked unavailable because their original source cannot establish whether they were recorded. Explicit new zero values remain valid. Older database versions require explicit migrations; destructive fallback is disabled. Supabase stores accounts and onboarding profile metadata; logs remain local to this device. The existing single-account model clears local data when switching accounts.

For a larger catalog, place a UTF-8 `foods.csv` at `app/src/main/assets/foods.csv` with `food_name,calories,protein,carbs,fat`; optional `fiber,sugar,sodium_mg,serving_size,serving_unit` are supported. Include the actual nutrition basis so quantity scaling is meaningful. Missing macro columns are marked unavailable. Up to 5,000 rows plus built-in estimate foods are supported; no larger dataset is currently bundled. Manual logging lets the user enter packaging values for their entire portion.

Login supports Android Credential Manager: Use Saved Password retrieves from the configured device provider; Save with password manager offers saving after Supabase succeeds. Passwords are not stored by KcalFit. Refresh tokens are encrypted with AES-GCM using Android Keystore and stored outside backup, with migration from the former preferences token. Sign-out clears app/provider state and attempts Supabase session revocation. Provider prompts and live authentication must be checked on the target device.
