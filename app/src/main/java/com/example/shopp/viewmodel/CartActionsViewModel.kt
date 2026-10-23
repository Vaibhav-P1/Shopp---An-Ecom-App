package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Add-to-cart for screens that only add items (product lists, details, favorites). */
@HiltViewModel
class CartActionsViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun addToCart(productId: String) {
        viewModelScope.launch {
            cartRepository.addItem(productId).fold(
                onSuccess = { _messages.send("Item added to the cart") },
                onFailure = { _messages.send(it.localizedMessage ?: "Failed adding item to the cart") }
            )
        }
    }
}
