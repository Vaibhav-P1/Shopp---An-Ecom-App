package com.example.shopp.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface PaymentResult {
    data class Success(val paymentId: String) : PaymentResult
    data class Failure(val message: String) : PaymentResult
}

/**
 * Carries Razorpay's callbacks, which arrive on the Activity, to whichever ViewModel started the payment.
 * Results are not replayed, so a late callback can never complete a different checkout.
 */
@Singleton
class PaymentResultBus @Inject constructor() {

    private val _results = MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val results: SharedFlow<PaymentResult> = _results.asSharedFlow()

    fun publish(result: PaymentResult) {
        _results.tryEmit(result)
    }
}
