package com.example.shopp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shopp.OrderPlaced
import com.example.shopp.data.OrderRepository
import com.example.shopp.data.UserRepository
import com.example.shopp.model.OrderModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the order that was just placed, and the buyer's first name, for the confirmation screen. */
@HiltViewModel
class OrderPlacedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: OrderRepository,
    userRepository: UserRepository
) : ViewModel() {

    val orderId: String = savedStateHandle.toRoute<OrderPlaced>().orderId

    /** The order, or null while it loads or if it cannot be read; the screen still shows the id. */
    val order: StateFlow<OrderModel?> = repository.observeOrder(orderId)
        .catch { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getFirstName().onSuccess { _firstName.value = it }
        }
    }
}
