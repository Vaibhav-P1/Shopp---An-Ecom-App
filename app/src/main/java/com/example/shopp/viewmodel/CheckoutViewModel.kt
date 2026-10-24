package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.AddressRepository
import com.example.shopp.data.CartRepository
import com.example.shopp.data.CouponRepository
import com.example.shopp.data.OrderRepository
import com.example.shopp.data.PaymentMethod
import com.example.shopp.data.ProductRepository
import com.example.shopp.model.AddressModel
import com.example.shopp.domain.Coupon
import com.example.shopp.domain.CouponResult
import com.example.shopp.domain.CouponRules
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    val lines: List<CheckoutLine>,
    val subtotalPaise: Long
) {
    val hasUnavailableItems: Boolean get() = lines.any { it.unitPricePaise == null }
    val hasOrderableItems: Boolean get() = lines.isNotEmpty() && !hasUnavailableItems
}

data class CheckoutUiState(
    val content: UiState<CheckoutData> = UiState.Loading,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val isPlacing: Boolean = false,
    /** The primary saved address, which the order is delivered to; null when the user has none. */
    val address: AddressModel? = null,
    val couponInput: String = "",
    val appliedCoupon: AppliedCoupon? = null,
    val couponError: String? = null,
    val isApplyingCoupon: Boolean = false
) {
    val subtotalPaise: Long get() = (content as? UiState.Success)?.data?.subtotalPaise ?: 0L
    val breakdown: PriceBreakdown
        get() = PriceCalculator.calculate(subtotalPaise, appliedCoupon?.discountPaise ?: 0L)
    val canOrder: Boolean get() = (content as? UiState.Success)?.data?.hasOrderableItems == true && address != null
}

/** A coupon accepted for this checkout together with the discount it gives on the current subtotal. */
data class AppliedCoupon(val coupon: Coupon, val discountPaise: Long)

sealed interface CheckoutEvent {
    data class StartPayment(val amountPaise: Long, val orderId: String) : CheckoutEvent
    data class OrderPlaced(val orderId: String) : CheckoutEvent
    data class Message(val text: String) : CheckoutEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val addressRepository: AddressRepository,
    private val couponRepository: CouponRepository,
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
        observeAddress()
        viewModelScope.launch {
            paymentResultBus.results.collect { result ->
                if (!_uiState.value.isPlacing) return@collect
                when (result) {
                    is PaymentResult.Success -> {
                        paidPaymentId = result.paymentId
                        _uiState.value.address?.let { submit(it, PaymentMethod.ONLINE, result.paymentId) }
                            ?: _uiState.update { it.copy(isPlacing = false) }
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

    fun onCouponInputChange(value: String) {
        _uiState.update { it.copy(couponInput = value, couponError = null) }
    }

    fun applyCoupon() {
        val state = _uiState.value
        if (state.isApplyingCoupon || state.isPlacing) return
        _uiState.update { it.copy(isApplyingCoupon = true, couponError = null) }
        viewModelScope.launch {
            val outcome = couponRepository.getCoupon(state.couponInput).mapCatching { coupon ->
                when (val result = CouponRules.evaluate(coupon, state.subtotalPaise, System.currentTimeMillis())) {
                    is CouponResult.Applied -> AppliedCoupon(coupon, result.discountPaise)
                    is CouponResult.Rejected -> throw IllegalStateException(result.reason)
                }
            }
            _uiState.update {
                it.copy(
                    isApplyingCoupon = false,
                    appliedCoupon = outcome.getOrNull() ?: it.appliedCoupon,
                    couponInput = if (outcome.isSuccess) "" else it.couponInput,
                    couponError = outcome.exceptionOrNull()?.let { e -> e.localizedMessage ?: "Could not apply the coupon" }
                )
            }
        }
    }

    fun removeCoupon() {
        _uiState.update { it.copy(appliedCoupon = null, couponError = null) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun placeOrder() {
        val state = _uiState.value
        val address = state.address ?: return
        if (state.isPlacing || !state.canOrder) return

        _uiState.update { it.copy(isPlacing = true) }
        val alreadyPaid = paidPaymentId
        when {
            alreadyPaid != null -> submit(address, PaymentMethod.ONLINE, alreadyPaid)
            state.paymentMethod == PaymentMethod.COD -> submit(address, PaymentMethod.COD, null)
            else -> viewModelScope.launch {
                _events.send(CheckoutEvent.StartPayment(state.breakdown.totalPaise, orderId))
            }
        }
    }

    private fun submit(address: AddressModel, method: PaymentMethod, paymentId: String?) {
        viewModelScope.launch {
            orderRepository.placeOrder(orderId, address.id, _uiState.value.appliedCoupon?.coupon?.code, method, paymentId).fold(
                onSuccess = { _events.send(CheckoutEvent.OrderPlaced(orderId)) },
                onFailure = {
                    _uiState.update { state -> state.copy(isPlacing = false) }
                    _events.send(CheckoutEvent.Message(it.localizedMessage ?: "Could not place the order"))
                }
            )
        }
    }

    /** Follows the address book, so returning from "Change" shows the new primary address. */
    private fun observeAddress() {
        addressRepository.observeAddresses()
            .onEach { list ->
                val primary = list.firstOrNull { it.primary } ?: list.firstOrNull()
                _uiState.update { it.copy(address = primary) }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    private fun load() {
        viewModelScope.launch {
            val result = runCatching {
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
                CheckoutData(lines, subtotal)
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
