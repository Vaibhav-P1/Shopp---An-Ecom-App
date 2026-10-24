package com.example.shopp.domain

enum class CouponType { PERCENT, FLAT }

/**
 * A discount code. For [CouponType.PERCENT], [value] is a whole percentage (10 = 10%); for [CouponType.FLAT]
 * it is paise. A [maxDiscountPaise] of 0 means no cap, and a null [expiresAtMillis] means it never expires.
 */
data class Coupon(
    val code: String,
    val type: CouponType,
    val value: Long,
    val minOrderPaise: Long = 0,
    val maxDiscountPaise: Long = 0,
    val expiresAtMillis: Long? = null,
    val active: Boolean = true,
    val description: String = ""
)

sealed interface CouponResult {
    data class Applied(val discountPaise: Long) : CouponResult
    data class Rejected(val reason: String) : CouponResult
}

/** Coupon eligibility and discount maths, shared by the checkout screen and order placement. */
object CouponRules {

    fun evaluate(coupon: Coupon, subtotalPaise: Long, nowMillis: Long): CouponResult {
        if (!coupon.active) return CouponResult.Rejected("This coupon is not active")
        if (coupon.expiresAtMillis != null && nowMillis > coupon.expiresAtMillis) {
            return CouponResult.Rejected("This coupon has expired")
        }
        if (subtotalPaise < coupon.minOrderPaise) {
            return CouponResult.Rejected("Add ${Money.format(coupon.minOrderPaise - subtotalPaise)} more to use this coupon")
        }

        var discount = when (coupon.type) {
            CouponType.PERCENT -> (subtotalPaise * coupon.value.coerceIn(0, 100) * 100 + 5_000L) / 10_000L
            CouponType.FLAT -> coupon.value.coerceAtLeast(0)
        }
        if (coupon.maxDiscountPaise > 0) discount = minOf(discount, coupon.maxDiscountPaise)
        return CouponResult.Applied(minOf(discount, subtotalPaise))
    }
}
