package com.kcalfit.app.data.repository

import com.kcalfit.app.data.local.UserDao
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.preferences.UserPreferences
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {

    // --- Password Hashing ---
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // --- Register ---
    suspend fun register(name: String, email: String, password: String): AuthResult {
        if (name.isBlank()) return AuthResult.Error("Name cannot be empty")
        if (email.isBlank() || !email.contains("@")) return AuthResult.Error("Invalid email")
        if (password.length < 4) return AuthResult.Error("Password must be at least 4 characters")

        val existing = userDao.getUserProfile()
        if (existing != null && existing.email == email && existing.passwordHash.isNotEmpty()) {
            return AuthResult.Error("Account already exists. Please login.")
        }

        val hashed = hashPassword(password)
        val user = UserEntity(
            id = 1,
            name = name.trim(),
            email = email.trim(),
            passwordHash = hashed,
            isLoggedIn = true,
            isOnboarded = false
        )
        userDao.insertOrUpdateUser(user)
        userPreferences.setLoggedIn(true)
        userPreferences.setUserEmail(email.trim())
        return AuthResult.Success(user)
    }

    // --- Login ---
    suspend fun login(email: String, password: String): AuthResult {
        if (email.isBlank()) return AuthResult.Error("Email cannot be empty")
        if (password.isBlank()) return AuthResult.Error("Password cannot be empty")

        val user = userDao.getUserProfile()
            ?: return AuthResult.Error("No account found. Please register first.")

        if (user.email != email.trim()) {
            return AuthResult.Error("Email does not match registered account.")
        }

        val hashed = hashPassword(password)
        if (user.passwordHash != hashed) {
            return AuthResult.Error("Incorrect password.")
        }

        userDao.setLoggedIn(true)
        userPreferences.setLoggedIn(true)
        return AuthResult.Success(user)
    }

    // --- Logout ---
    suspend fun logout() {
        userDao.setLoggedIn(false)
        userPreferences.setLoggedIn(false)
    }

    // --- Session Check ---
    fun isLoggedIn(): Boolean = userPreferences.isLoggedIn()
    fun isOnboarded(): Boolean = userPreferences.isOnboarded()

    suspend fun getUserProfile(): UserEntity? = userDao.getUserProfile()
}

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
