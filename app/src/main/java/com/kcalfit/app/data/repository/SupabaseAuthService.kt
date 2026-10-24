package com.kcalfit.app.data.repository

import com.kcalfit.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Supabase Auth's public REST API. No service-role key or local OTP is used. */
class SupabaseAuthService {
    class AuthHttpException(val status: Int, message: String, val errorCode: String = "") : IllegalStateException(message)
    data class Session(val userId: String, val email: String, val name: String, val accessToken: String, val refreshToken: String, val metadata: JSONObject)
    companion object { const val APP_AUTH_REDIRECT = "com.kcalfit.app://auth/callback" }

    private val baseUrl get() = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val key get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    private suspend fun post(path: String, body: JSONObject): JSONObject = request("POST", path, body)

    private suspend fun request(method: String, path: String, body: JSONObject, accessToken: String? = null): JSONObject = withContext(Dispatchers.IO) {
        if (!baseUrl.startsWith("https://") || key.isBlank()) {
            throw IllegalStateException("Email authentication is not configured. Add SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY to local.properties.")
        }
        val connection = (URL("$baseUrl/auth/v1/$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = method != "GET"
            setRequestProperty("apikey", key)
            setRequestProperty("Content-Type", "application/json")
            if (accessToken != null) setRequestProperty("Authorization", "Bearer $accessToken")
        }
        try {
            if (method != "GET") connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = if (response.isBlank()) JSONObject() else JSONObject(response)
            if (connection.responseCode !in 200..299) {
                val code = json.optString("error_code")
                val message = if (code == "user_already_exists" || code == "email_exists") "An account already exists." else json.optString("msg", json.optString("message", "Authentication failed"))
                throw AuthHttpException(connection.responseCode, message, code)
            }
            json
        } finally {
            connection.disconnect()
        }
    }

    suspend fun register(name: String, email: String, password: String) {
        val response = post(
            "signup?redirect_to=${encode(APP_AUTH_REDIRECT)}",
            JSONObject().put("email", email).put("password", password).put("data", JSONObject().put("name", name))
        )
        // Supabase can deliberately obfuscate an already registered account. With
        // email confirmation enabled, a duplicate has an empty identities array.
        val user = response.optJSONObject("user") ?: response
        if (user.has("identities") && user.optJSONArray("identities")?.length() == 0) {
            throw IllegalStateException("An account already exists.")
        }
    }

    suspend fun verify(email: String, code: String): Session = session(
        post("verify", JSONObject().put("email", email).put("token", code).put("type", "email"))
    )

    suspend fun resend(email: String) {
        post("resend?redirect_to=${encode(APP_AUTH_REDIRECT)}", JSONObject().put("email", email).put("type", "signup"))
    }

    suspend fun requestPasswordReset(email: String) {
        post("recover?redirect_to=${encode(APP_AUTH_REDIRECT)}", JSONObject().put("email", email))
    }

    /** Exchanges a Google ID token from Credential Manager; [nonce] is the raw value whose SHA-256 Google signed. */
    suspend fun signInWithGoogleIdToken(idToken: String, nonce: String): Session = session(
        post("token?grant_type=id_token", JSONObject().put("provider", "google").put("id_token", idToken).put("nonce", nonce))
    )

    suspend fun sessionFromOAuthTokens(accessToken: String, refreshToken: String): Session {
        val user = request("GET", "user", JSONObject(), accessToken)
        val metadata = user.optJSONObject("user_metadata") ?: JSONObject()
        return Session(user.getString("id"), user.optString("email"),
            metadata.optString("name", metadata.optString("full_name")), accessToken, refreshToken, metadata)
    }

    suspend fun updatePassword(accessToken: String, newPassword: String) {
        request("PUT", "user", JSONObject().put("password", newPassword), accessToken)
    }

    suspend fun login(email: String, password: String): Session = session(
        post("token?grant_type=password", JSONObject().put("email", email).put("password", password))
    )

    suspend fun refresh(refreshToken: String): Session = session(
        post("token?grant_type=refresh_token", JSONObject().put("refresh_token", refreshToken))
    )

    suspend fun signOut(refreshToken: String) {
        if (refreshToken.isNotBlank()) {
            val current = refresh(refreshToken)
            request("POST", "logout?scope=local", JSONObject(), current.accessToken)
        }
    }

    suspend fun updateProfile(refreshToken: String, metadata: JSONObject, onRefreshed: (Session) -> Unit) {
        val current = refresh(refreshToken)
        onRefreshed(current)
        request("PUT", "user", JSONObject().put("data", metadata), current.accessToken)
    }

    private fun session(response: JSONObject): Session {
        val user = response.optJSONObject("user") ?: throw IllegalStateException("Authentication returned no user")
        val accessToken = response.optString("access_token")
        val refreshToken = response.optString("refresh_token")
        if (accessToken.isBlank() || refreshToken.isBlank()) throw IllegalStateException("Authentication returned no session")
        return Session(
            userId = user.getString("id"),
            email = user.optString("email"),
            name = user.optJSONObject("user_metadata")?.optString("name").orEmpty(),
            accessToken = accessToken,
            refreshToken = refreshToken,
            metadata = user.optJSONObject("user_metadata") ?: JSONObject()
        )
    }

    private fun encode(value: String) = URLEncoder.encode(value, Charsets.UTF_8.name())
}
