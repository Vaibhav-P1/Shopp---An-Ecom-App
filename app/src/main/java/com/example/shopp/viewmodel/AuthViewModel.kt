package com.example.shopp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

enum class AuthField { Name, Email, Password }

sealed interface AuthEvent {
    data object Authenticated : AuthEvent
    data class Failure(val message: String) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val isLoggedIn: Boolean get() = repository.isLoggedIn

    fun login(email: String, password: String) {
        val trimmed = email.trim()
        if (!validate(trimmed, password)) return
        submit { repository.login(trimmed, password) }
    }

    fun signup(email: String, name: String, password: String) {
        val trimmed = email.trim()
        if (!validate(trimmed, password, name)) return
        submit { repository.signup(trimmed, name.trim(), password) }
    }

    fun signInWithGoogle(idToken: String) = submit { repository.signInWithGoogle(idToken) }

    fun onGoogleFailure(message: String?) = fail(message ?: "Google Sign-In failed")

    fun onGoogleStarted() {
        _uiState.value = AuthUiState(isLoading = true)
    }

    /** Clears the inline error of a field once the user edits it. */
    fun clearError(field: AuthField) {
        _uiState.update {
            when (field) {
                AuthField.Name -> it.copy(nameError = null)
                AuthField.Email -> it.copy(emailError = null)
                AuthField.Password -> it.copy(passwordError = null)
            }
        }
    }

    /** Publishes inline field errors and returns whether the input is valid. [name] is null on login. */
    private fun validate(email: String, password: String, name: String? = null): Boolean {
        val state = AuthUiState(
            nameError = if (name != null && name.isBlank()) "Please enter your name" else null,
            emailError = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else "Please enter a valid email",
            passwordError = if (password.length >= 6) null else "Password must be at least 6 characters"
        )
        _uiState.value = state
        return state.nameError == null && state.emailError == null && state.passwordError == null
    }

    private fun fail(message: String) {
        _uiState.value = AuthUiState(isLoading = false)
        _events.trySend(AuthEvent.Failure(message))
    }

    private fun submit(block: suspend () -> Result<Unit>) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            block()
                .onSuccess {
                    _uiState.value = AuthUiState(isLoading = false)
                    _events.send(AuthEvent.Authenticated)
                }
                .onFailure { fail(it.localizedMessage ?: "Something went wrong") }
        }
    }
}
