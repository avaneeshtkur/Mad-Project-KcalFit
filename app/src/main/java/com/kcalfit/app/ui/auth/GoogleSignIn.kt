package com.kcalfit.app.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kcalfit.app.BuildConfig
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Native "Sign in with Google" through Credential Manager. The ID token is exchanged with Supabase by the caller. */
class GoogleSignIn(context: Context) {
    sealed interface Result {
        /** [nonce] is the raw nonce; Google received only its SHA-256 hash. */
        data class Success(val idToken: String, val nonce: String) : Result
        data object Cancelled : Result
        data class Failure(val message: String) : Result
    }

    private val manager = CredentialManager.create(context)

    /** [activityContext] must be an Activity so the account picker can be shown. */
    suspend fun signIn(activityContext: Context): Result {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank()) return Result.Failure("Google sign-in is not configured. Add GOOGLE_WEB_CLIENT_ID to local.properties.")
        val nonce = newNonce()
        val option = GetSignInWithGoogleOption.Builder(clientId).setNonce(sha256(nonce)).build()
        val credential = try {
            manager.getCredential(activityContext, GetCredentialRequest(listOf(option))).credential
        } catch (_: GetCredentialCancellationException) {
            return Result.Cancelled
        } catch (_: NoCredentialException) {
            return Result.Failure("No Google account is available. Add a Google account to this device and try again.")
        } catch (e: GetCredentialException) {
            return Result.Failure(e.errorMessage?.toString()?.takeIf { it.isNotBlank() } ?: "Google sign-in is unavailable on this device.")
        }
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return Result.Failure("Google returned an unexpected credential. Please try again.")
        }
        return try {
            Result.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken, nonce)
        } catch (_: GoogleIdTokenParsingException) {
            Result.Failure("Google returned an invalid sign-in token. Please try again.")
        }
    }

    private fun newNonce(): String = ByteArray(32).also(SecureRandom()::nextBytes)
        .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
