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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(val isLoading: Boolean = false)

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
        validateCredentials(trimmed, password)?.let { return fail(it) }
        submit { repository.login(trimmed, password) }
    }

    fun signup(email: String, name: String, password: String) {
        val trimmed = email.trim()
        if (name.isBlank()) return fail("Please enter your name")
        validateCredentials(trimmed, password)?.let { return fail(it) }
        submit { repository.signup(trimmed, name.trim(), password) }
    }

    fun signInWithGoogle(idToken: String) = submit { repository.signInWithGoogle(idToken) }

    fun onGoogleFailure(message: String?) = fail(message ?: "Google Sign-In failed")

    fun onGoogleStarted() {
        _uiState.value = AuthUiState(isLoading = true)
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
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
