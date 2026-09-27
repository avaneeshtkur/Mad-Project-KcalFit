package com.kcalfit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kcalfit.app.data.repository.AuthRepository
import com.kcalfit.app.data.repository.AuthResult
import com.kcalfit.app.data.repository.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val currentUser: AuthUser? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = authRepository.isUserLoggedIn(),
            isOnboardingCompleted = authRepository.isOnboardingCompleted(),
            currentUser = authRepository.getCurrentUser()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isLoggedIn = true,
                            isOnboardingCompleted = authRepository.isOnboardingCompleted(),
                            currentUser = result.data
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        onSuccess: () -> Unit
    ) {
        if (displayName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name.") }
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(email, password, displayName)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isLoggedIn = true,
                            isOnboardingCompleted = false,
                            currentUser = result.data
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.resetPassword(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "Password reset link sent to your email."
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState(
                    isLoggedIn = false,
                    isOnboardingCompleted = false,
                    currentUser = null
                )
            }
            onSuccess()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
