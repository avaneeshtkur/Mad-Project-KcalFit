package com.kcalfit.app.ui.auth

import android.content.Context
import androidx.credentials.*

class PasswordManager(context: Context) {
    private val manager = CredentialManager.create(context)
    suspend fun retrieve(context: Context): PasswordCredential? {
        val response = manager.getCredential(context, GetCredentialRequest(listOf(GetPasswordOption())))
        return response.credential as? PasswordCredential
    }
    suspend fun save(context: Context, email: String, password: String) {
        manager.createCredential(context, CreatePasswordRequest(email, password))
    }
    suspend fun signOut() { manager.clearCredentialState(ClearCredentialStateRequest()) }
}
