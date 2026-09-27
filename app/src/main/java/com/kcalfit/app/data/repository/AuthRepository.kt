package com.kcalfit.app.data.repository

import com.kcalfit.app.data.local.UserDao
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String
)

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<AuthUser>
    suspend fun register(email: String, password: String, displayName: String): AuthResult<AuthUser>
    suspend fun resetPassword(email: String): AuthResult<Unit>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun isOnboardingCompleted(): Boolean
    fun getCurrentUser(): AuthUser?
    fun getCurrentUserId(): String?
}

class LocalAuthRepository(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : AuthRepository {

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override suspend fun login(email: String, password: String): AuthResult<AuthUser> =
        withContext(Dispatchers.IO) {
            try {
                val cleanEmail = email.trim().lowercase()
                val cleanPass = password.trim()

                if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                    return@withContext AuthResult.Error("Please enter a valid email address.")
                }
                if (cleanPass.length < 6) {
                    return@withContext AuthResult.Error("Password must be at least 6 characters.")
                }

                val existingUser = userDao.getUserProfile()
                val passwordHash = hashPassword(cleanPass)

                if (existingUser != null && existingUser.email.equals(cleanEmail, ignoreCase = true)) {
                    // Check password if set, otherwise accept if existing record had empty hash
                    if (existingUser.passwordHash.isNotEmpty() && existingUser.passwordHash != passwordHash) {
                        return@withContext AuthResult.Error("Invalid email or password.")
                    }

                    val updatedUser = existingUser.copy(
                        isLoggedIn = true
                    )
                    userDao.insertOrUpdateUser(updatedUser)

                    val uid = if (updatedUser.firebaseUid.isNotBlank()) updatedUser.firebaseUid else UUID.randomUUID().toString()
                    userPreferences.setLoggedIn(true)
                    userPreferences.setLoggedInEmail(cleanEmail)
                    userPreferences.setUserId(uid)
                    userPreferences.setDisplayName(updatedUser.name)
                    userPreferences.setOnboardingCompleted(updatedUser.isOnboarded)

                    return@withContext AuthResult.Success(
                        AuthUser(
                            uid = uid,
                            email = cleanEmail,
                            displayName = updatedUser.name
                        )
                    )
                } else {
                    // First time login or new user login without prior registration
                    val uid = UUID.randomUUID().toString()
                    val newUser = (existingUser ?: UserEntity()).copy(
                        id = 1,
                        name = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = cleanEmail,
                        passwordHash = passwordHash,
                        isLoggedIn = true,
                        isOnboarded = false,
                        firebaseUid = uid
                    )
                    userDao.insertOrUpdateUser(newUser)

                    userPreferences.setLoggedIn(true)
                    userPreferences.setLoggedInEmail(cleanEmail)
                    userPreferences.setUserId(uid)
                    userPreferences.setDisplayName(newUser.name)
                    userPreferences.setOnboardingCompleted(false)

                    return@withContext AuthResult.Success(
                        AuthUser(
                            uid = uid,
                            email = cleanEmail,
                            displayName = newUser.name
                        )
                    )
                }
            } catch (e: Exception) {
                return@withContext AuthResult.Error(e.localizedMessage ?: "An unexpected error occurred during login.")
            }
        }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): AuthResult<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim().lowercase()
            val cleanPass = password.trim()
            val cleanName = displayName.trim().ifBlank { "Fitness User" }

            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                return@withContext AuthResult.Error("Please enter a valid email address.")
            }
            if (cleanPass.length < 6) {
                return@withContext AuthResult.Error("Password must be at least 6 characters.")
            }

            val uid = UUID.randomUUID().toString()
            val passwordHash = hashPassword(cleanPass)

            val existingProfile = userDao.getUserProfile()
            val newUser = (existingProfile ?: UserEntity()).copy(
                id = 1,
                name = cleanName,
                email = cleanEmail,
                passwordHash = passwordHash,
                isLoggedIn = true,
                isOnboarded = false,
                firebaseUid = uid
            )
            userDao.insertOrUpdateUser(newUser)

            userPreferences.setLoggedIn(true)
            userPreferences.setLoggedInEmail(cleanEmail)
            userPreferences.setUserId(uid)
            userPreferences.setDisplayName(cleanName)
            userPreferences.setOnboardingCompleted(false)

            return@withContext AuthResult.Success(
                AuthUser(
                    uid = uid,
                    email = cleanEmail,
                    displayName = cleanName
                )
            )
        } catch (e: Exception) {
            return@withContext AuthResult.Error(e.localizedMessage ?: "Failed to create account.")
        }
    }

    override suspend fun resetPassword(email: String): AuthResult<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        // In local mode, simulate successful password reset email dispatched
        return@withContext AuthResult.Success(Unit)
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            userDao.setLoggedIn(false)
        } catch (_: Exception) {}
        userPreferences.clearSession()
    }

    override fun isUserLoggedIn(): Boolean {
        return userPreferences.isLoggedIn()
    }

    override fun isOnboardingCompleted(): Boolean {
        return userPreferences.isOnboardingCompleted()
    }

    override fun getCurrentUser(): AuthUser? {
        val uid = userPreferences.getUserId() ?: return null
        val email = userPreferences.getLoggedInEmail() ?: ""
        val name = userPreferences.getDisplayName()
        return AuthUser(uid = uid, email = email, displayName = name)
    }

    override fun getCurrentUserId(): String? {
        return userPreferences.getUserId()
    }
}
