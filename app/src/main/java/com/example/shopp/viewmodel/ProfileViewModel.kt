package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.AuthRepository
import com.example.shopp.data.CartRepository
import com.example.shopp.data.UserRepository
import com.example.shopp.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileData(val user: UserModel, val cartItemCount: Long)

sealed interface ProfileEvent {
    data class Message(val text: String) : ProfileEvent
    data object SignedOut : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProfileData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProfileData>> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = UiState.Loading
        load()
    }

    fun saveAddress(address: String) {
        if (address.isBlank()) {
            send(ProfileEvent.Message("Address can't be empty"))
            return
        }
        viewModelScope.launch {
            userRepository.updateAddress(address).fold(
                onSuccess = {
                    updateData { data -> data.copy(user = data.user.copy(address = address.trim())) }
                    send(ProfileEvent.Message("Address updated successfully"))
                },
                onFailure = { send(ProfileEvent.Message(it.localizedMessage ?: "Could not update the address")) }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        send(ProfileEvent.SignedOut)
    }

    private fun load() {
        viewModelScope.launch {
            userRepository.getProfile().fold(
                onSuccess = { user ->
                    _uiState.value = UiState.Success(ProfileData(user, user.cartItems.values.sum()))
                    observeCartCount()
                },
                onFailure = { _uiState.value = UiState.Error(it.localizedMessage ?: "Something went wrong") }
            )
        }
    }

    /** Keeps the cart count live, so it is right after returning from the cart tab. */
    private fun observeCartCount() {
        cartRepository.observeCart()
            .onEach { cart -> updateData { data -> data.copy(cartItemCount = cart.values.sum()) } }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun updateData(transform: (ProfileData) -> ProfileData) {
        val current = _uiState.value as? UiState.Success ?: return
        _uiState.value = UiState.Success(transform(current.data))
    }

    private fun send(event: ProfileEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
