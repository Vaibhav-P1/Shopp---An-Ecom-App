package com.example.shopp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shopp.OrderDetails
import com.example.shopp.data.OrderRepository
import com.example.shopp.model.OrderModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OrderRepository
) : ViewModel() {

    private val orderId: String = savedStateHandle.toRoute<OrderDetails>().orderId

    private val reload = MutableStateFlow(0)

    val uiState: StateFlow<UiState<OrderModel>> = reload
        .flatMapLatest {
            repository.observeOrder(orderId)
                .map<OrderModel, UiState<OrderModel>> { UiState.Success(it) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling: StateFlow<Boolean> = _isCancelling

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun retry() {
        reload.update { it + 1 }
    }

    fun cancelOrder() {
        if (_isCancelling.value) return
        _isCancelling.value = true
        viewModelScope.launch {
            repository.cancelOrder(orderId).fold(
                onSuccess = { _messages.send("Order cancelled") },
                onFailure = { _messages.send(it.localizedMessage ?: "Could not cancel the order") }
            )
            _isCancelling.value = false
        }
    }
}
