package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.AuthRepository
import com.example.shopp.data.CartRepository
import com.example.shopp.data.FavoritesRepository
import com.example.shopp.data.OrderRepository
import com.example.shopp.data.ThemePreferenceRepository
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

data class ProfileData(
    val user: UserModel,
    val cartItemCount: Long,
    val orderCount: Int = 0,
    val favoriteCount: Int = 0
)

sealed interface ProfileEvent {
    data class Message(val text: String) : ProfileEvent
    data object SignedOut : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val favoritesRepository: FavoritesRepository,
    private val themePreferences: ThemePreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProfileData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProfileData>> = _uiState.asStateFlow()

    /** Explicit dark-mode choice, or null while following the system. */
    val darkTheme: StateFlow<Boolean?> = themePreferences.dark

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = UiState.Loading
        load()
    }

    fun signOut() {
        authRepository.signOut()
        send(ProfileEvent.SignedOut)
    }

    fun setDarkTheme(enabled: Boolean) = themePreferences.setDark(enabled)

    private fun load() {
        viewModelScope.launch {
            userRepository.getProfile().fold(
                onSuccess = { user ->
                    // Profiles created by Google sign-in can have blank fields, so fall back to the account
                    val named = user.copy(
                        name = user.name.ifBlank { authRepository.displayName.orEmpty() },
                        email = user.email.ifBlank { authRepository.email.orEmpty() }
                    )
                    _uiState.value = UiState.Success(ProfileData(named, user.cartItems.values.sum()))
                    observeCounts()
                },
                onFailure = { _uiState.value = UiState.Error(it.localizedMessage ?: "Something went wrong") }
            )
        }
    }

    /** Keeps the cart, order and favourite counts live, so they are right after coming back to the tab. */
    private fun observeCounts() {
        cartRepository.observeCart()
            .onEach { cart -> updateData { data -> data.copy(cartItemCount = cart.values.sum()) } }
            .catch { }
            .launchIn(viewModelScope)
        orderRepository.observeOrders()
            .onEach { orders -> updateData { data -> data.copy(orderCount = orders.size) } }
            .catch { }
            .launchIn(viewModelScope)
        favoritesRepository.observeFavorites()
            .onEach { ids -> updateData { data -> data.copy(favoriteCount = ids.size) } }
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
