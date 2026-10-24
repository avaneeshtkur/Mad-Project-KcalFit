package com.kcalfit.app.data.repository

import java.net.URLDecoder

/**
 * A Supabase Auth redirect to [SupabaseAuthService.APP_AUTH_REDIRECT]. Supabase's implicit flow
 * returns the session in the URI fragment; errors may arrive in the fragment or the query.
 * Plain-string parsing keeps this independent of android.net.Uri so it can be unit tested.
 */
sealed interface AuthCallback {
    /** Signup confirmation, magic link, invite or email change: an ordinary sign-in session. */
    data class SignIn(val accessToken: String, val refreshToken: String) : AuthCallback
    /** Password recovery: the session may only be used to choose a new password. */
    data class Recovery(val accessToken: String, val refreshToken: String) : AuthCallback
    data class Failure(val message: String) : AuthCallback
    /** Not a Supabase auth redirect (e.g. a launcher intent). */
    data object NotAuth : AuthCallback

    companion object {
        private const val EXPIRED = "This link is invalid or has expired. Request a new email and try again."
        private const val INCOMPLETE = "The authentication link is incomplete or expired. Request a new link and try again."

        fun parse(uri: String?): AuthCallback {
            if (uri.isNullOrBlank() || !uri.startsWith(SupabaseAuthService.APP_AUTH_REDIRECT)) return NotAuth
            val query = uri.substringAfter('?', "").substringBefore('#')
            val fragment = uri.substringAfter('#', "")
            val params = params(query) + params(fragment)
            if (params["error_code"] != null || params["error"] != null) {
                return Failure(if (params["error_code"] == "otp_expired") EXPIRED else params["error_description"] ?: INCOMPLETE)
            }
            val access = params["access_token"].orEmpty()
            val refresh = params["refresh_token"].orEmpty()
            if (access.isBlank() || refresh.isBlank()) return Failure(INCOMPLETE)
            return if (params["type"] == "recovery") Recovery(access, refresh) else SignIn(access, refresh)
        }

        private fun params(raw: String): Map<String, String> = raw.split('&')
            .filter { it.contains('=') }
            .associate { pair ->
                val (key, value) = pair.split('=', limit = 2)
                decode(key) to decode(value)
            }

        private fun decode(value: String) = URLDecoder.decode(value, Charsets.UTF_8.name())
    }
}
