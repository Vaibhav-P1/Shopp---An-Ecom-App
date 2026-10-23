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

    // Rates are in basis points (1% = 100 bps). Coupons replace the flat discount in Phase B.
    private const val DISCOUNT_BPS = 1_000L
    private const val TAX_BPS = 1_300L

    fun calculate(subtotalPaise: Long): PriceBreakdown = PriceBreakdown(
        subtotalPaise = subtotalPaise,
        discountPaise = percentOf(subtotalPaise, DISCOUNT_BPS),
        taxPaise = percentOf(subtotalPaise, TAX_BPS)
    )

    /** `amount * bps / 10000`, rounded half up. */
    private fun percentOf(amountPaise: Long, bps: Long): Long = (amountPaise * bps + 5_000L) / 10_000L
}
