package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.CartRepository
import com.example.shopp.data.ProductRepository
import com.example.shopp.model.ProductModel
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A cart row. [product] is null when the product no longer exists in the catalogue. */
data class CartLine(val productId: String, val product: ProductModel?, val quantity: Long)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productCache = mutableMapOf<String, ProductModel>()
    private val reload = MutableStateFlow(0)

    val uiState: StateFlow<UiState<List<CartLine>>> = reload
        .flatMapLatest {
            cartRepository.observeCart()
                .map<Map<String, Long>, UiState<List<CartLine>>> { cart -> UiState.Success(toLines(cart)) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun retry() {
        reload.value++
    }

    fun increment(productId: String) = mutate("Failed updating the cart") { cartRepository.addItem(productId) }

    fun decrement(productId: String) = mutate("Failed updating the cart") { cartRepository.decrementItem(productId) }

    fun remove(productId: String) = mutate("Failed removing item from the cart") { cartRepository.removeItem(productId) }

    private suspend fun toLines(cart: Map<String, Long>): List<CartLine> {
        val missing = cart.keys - productCache.keys
        if (missing.isNotEmpty()) {
            productRepository.getProductsByIds(missing).getOrThrow().forEach { productCache[it.id] = it }
        }
        return cart.map { (id, quantity) -> CartLine(id, productCache[id], quantity) }
            .sortedWith(compareBy({ it.product?.title ?: "~" }, { it.productId }))
    }

    private fun mutate(failureMessage: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            block().onFailure { _messages.send(it.localizedMessage ?: failureMessage) }
        }
    }
}
