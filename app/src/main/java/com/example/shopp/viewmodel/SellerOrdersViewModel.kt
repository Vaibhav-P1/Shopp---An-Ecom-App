package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.AuthRepository
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
class SellerOrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val sellerId: String = authRepository.currentUserId.orEmpty()

    private val reload = MutableStateFlow(0)

    val orders: StateFlow<UiState<List<OrderModel>>> = reload
        .flatMapLatest {
            repository.observeSellerOrders()
                .map<List<OrderModel>, UiState<List<OrderModel>>> { UiState.Success(it) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Ids of orders whose status change is in flight, so a double tap cannot skip a step. */
    private val _updating = MutableStateFlow<Set<String>>(emptySet())
    val updating: StateFlow<Set<String>> = _updating

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun retry() {
        reload.update { it + 1 }
    }

    fun advance(order: OrderModel) {
        if (order.id in _updating.value) return
        _updating.update { it + order.id }
        viewModelScope.launch {
            repository.advanceStatus(order.id).onFailure {
                _messages.send(it.localizedMessage ?: "Could not update the order")
            }
            _updating.update { it - order.id }
        }
    }
}
