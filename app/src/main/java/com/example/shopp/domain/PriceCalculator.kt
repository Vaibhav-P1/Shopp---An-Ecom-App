package com.example.shopp.domain

data class PriceBreakdown(
    val subtotalPaise: Long,
    val discountPaise: Long,
    val taxPaise: Long
) {
    val totalPaise: Long get() = subtotalPaise - discountPaise + taxPaise
}

/** The single place where order totals are computed, shared by the checkout screen and order placement. */
object PriceCalculator {

    // Rates are in basis points (1% = 100 bps)
    private const val TAX_BPS = 1_300L

    /** Tax is charged on the price after [discountPaise], which is capped at the subtotal. */
    fun calculate(subtotalPaise: Long, discountPaise: Long = 0): PriceBreakdown {
        val discount = discountPaise.coerceIn(0, subtotalPaise)
        return PriceBreakdown(
            subtotalPaise = subtotalPaise,
            discountPaise = discount,
            taxPaise = percentOf(subtotalPaise - discount, TAX_BPS)
        )
    }

    /** `amount * bps / 10000`, rounded half up. */
    private fun percentOf(amountPaise: Long, bps: Long): Long = (amountPaise * bps + 5_000L) / 10_000L
}
