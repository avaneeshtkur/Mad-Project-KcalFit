package com.kcalfit.app.data.repository

import org.junit.Assert.*
import org.junit.Test

class AuthCallbackTest {
    private val base = SupabaseAuthService.APP_AUTH_REDIRECT

    @Test fun signupConfirmationFragmentIsASignIn() {
        assertEquals(AuthCallback.SignIn("a.b.c", "r1"),
            AuthCallback.parse("$base#access_token=a.b.c&expires_in=3600&refresh_token=r1&token_type=bearer&type=signup"))
    }

    @Test fun recoveryIsDistinguishedFromSignIn() {
        assertEquals(AuthCallback.Recovery("a", "r"), AuthCallback.parse("$base#access_token=a&refresh_token=r&type=recovery"))
    }

    @Test fun expiredLinkGivesAnExpiryMessage() {
        val result = AuthCallback.parse("$base#error=access_denied&error_code=otp_expired&error_description=Email+link+is+invalid+or+has+expired")
        assertTrue((result as AuthCallback.Failure).message.contains("expired"))
    }

    @Test fun queryErrorsUseTheDecodedDescription() {
        assertEquals(AuthCallback.Failure("Unable to exchange external code"),
            AuthCallback.parse("$base?error=server_error&error_description=Unable+to+exchange+external+code"))
    }

    @Test fun missingTokensAreIncomplete() {
        assertTrue(AuthCallback.parse("$base#access_token=a&type=signup") is AuthCallback.Failure)
        assertTrue(AuthCallback.parse(base) is AuthCallback.Failure)
    }

    @Test fun otherUrisAreIgnored() {
        assertEquals(AuthCallback.NotAuth, AuthCallback.parse(null))
        assertEquals(AuthCallback.NotAuth, AuthCallback.parse("https://example.com/#access_token=a&refresh_token=r"))
    }
}
