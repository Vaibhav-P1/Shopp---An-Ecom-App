package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.CartRepository
import com.example.shopp.data.OrderRepository
import com.example.shopp.data.PaymentMethod
import com.example.shopp.data.ProductRepository
import com.example.shopp.data.UserRepository
import com.example.shopp.domain.Money
import com.example.shopp.domain.PriceBreakdown
import com.example.shopp.domain.PriceCalculator
import com.example.shopp.payment.PaymentResult
import com.example.shopp.payment.PaymentResultBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** One row of the order summary. [unitPricePaise] is null when the product is gone or has no valid price. */
data class CheckoutLine(
    val productId: String,
    val title: String,
    val unitPricePaise: Long?,
    val quantity: Long
)

data class CheckoutData(
    val name: String,
    val address: String,
    val lines: List<CheckoutLine>,
    val breakdown: PriceBreakdown
) {
    val hasUnavailableItems: Boolean get() = lines.any { it.unitPricePaise == null }
    val canOrder: Boolean get() = lines.isNotEmpty() && !hasUnavailableItems && address.isNotBlank()
}

data class CheckoutUiState(
    val content: UiState<CheckoutData> = UiState.Loading,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val isPlacing: Boolean = false
)

sealed interface CheckoutEvent {
    data class StartPayment(val amountPaise: Long, val orderId: String) : CheckoutEvent
    data class OrderPlaced(val orderId: String) : CheckoutEvent
    data class Message(val text: String) : CheckoutEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val paymentResultBus: PaymentResultBus
) : ViewModel() {

    /** Created once per checkout, so retries and duplicate callbacks all refer to the same order. */
    private val orderId = "ORD_" + UUID.randomUUID().toString().replace("-", "").take(10).uppercase()

    /** Set once Razorpay confirms payment, so a failed save is retried without charging again. */
    private var paidPaymentId: String? = null

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = Channel<CheckoutEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
        viewModelScope.launch {
            paymentResultBus.results.collect { result ->
                if (!_uiState.value.isPlacing) return@collect
                when (result) {
                    is PaymentResult.Success -> {
                        paidPaymentId = result.paymentId
                        submit(PaymentMethod.ONLINE, result.paymentId)
                    }
                    is PaymentResult.Failure -> {
                        _uiState.update { it.copy(isPlacing = false) }
                        _events.send(CheckoutEvent.Message(result.message))
                    }
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(content = UiState.Loading) }
        load()
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun placeOrder() {
        val state = _uiState.value
        val data = (state.content as? UiState.Success)?.data ?: return
        if (state.isPlacing || !data.canOrder) return

        _uiState.update { it.copy(isPlacing = true) }
        val alreadyPaid = paidPaymentId
        when {
            alreadyPaid != null -> submit(PaymentMethod.ONLINE, alreadyPaid)
            state.paymentMethod == PaymentMethod.COD -> submit(PaymentMethod.COD, null)
            else -> viewModelScope.launch {
                _events.send(CheckoutEvent.StartPayment(data.breakdown.totalPaise, orderId))
            }
        }
    }

    private fun submit(method: PaymentMethod, paymentId: String?) {
        viewModelScope.launch {
            orderRepository.placeOrder(orderId, method, paymentId).fold(
                onSuccess = { _events.send(CheckoutEvent.OrderPlaced(orderId)) },
                onFailure = {
                    _uiState.update { state -> state.copy(isPlacing = false) }
                    _events.send(CheckoutEvent.Message(it.localizedMessage ?: "Could not place the order"))
                }
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            val result = runCatching {
                val profile = userRepository.getProfile().getOrThrow()
                val cart = cartRepository.getCart().getOrThrow()
                val products = productRepository.getProductsByIds(cart.keys).getOrThrow().associateBy { it.id }

                val lines = cart.map { (productId, quantity) ->
                    val product = products[productId]
                    CheckoutLine(
                        productId = productId,
                        title = product?.title ?: "Product no longer available",
                        unitPricePaise = product?.let { Money.parsePaise(it.actualPrice) },
                        quantity = quantity
                    )
                }.sortedBy { it.title }

                val subtotal = lines.sumOf { (it.unitPricePaise ?: 0L) * it.quantity }
                CheckoutData(profile.name, profile.address, lines, PriceCalculator.calculate(subtotal))
            }
            _uiState.update {
                it.copy(
                    content = result.fold(
                        onSuccess = { data -> UiState.Success(data) },
                        onFailure = { e -> UiState.Error(e.localizedMessage ?: "Something went wrong") }
                    )
                )
            }
        }
    }
}
