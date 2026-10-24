package com.example.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CouponRulesTest {

    private val now = 1_000_000L

    private fun percent(value: Long, min: Long = 0, cap: Long = 0, expires: Long? = null, active: Boolean = true) =
        Coupon("P", CouponType.PERCENT, value, min, cap, expires, active)

    private fun flat(paise: Long, min: Long = 0) = Coupon("F", CouponType.FLAT, paise, min)

    private fun discount(result: CouponResult) = (result as CouponResult.Applied).discountPaise

    @Test
    fun `percent coupon takes a share of the subtotal`() {
        assertEquals(30_000, discount(CouponRules.evaluate(percent(10), 300_000, now)))
    }

    @Test
    fun `percent discount respects the cap`() {
        assertEquals(20_000, discount(CouponRules.evaluate(percent(10, cap = 20_000), 300_000, now)))
    }

    @Test
    fun `flat coupon never exceeds the subtotal`() {
        assertEquals(20_000, discount(CouponRules.evaluate(flat(20_000), 300_000, now)))
        assertEquals(5_000, discount(CouponRules.evaluate(flat(20_000), 5_000, now)))
    }

    @Test
    fun `rejects inactive coupons`() {
        assertTrue(CouponRules.evaluate(percent(10, active = false), 300_000, now) is CouponResult.Rejected)
    }

    @Test
    fun `rejects expired coupons but accepts one expiring now`() {
        assertTrue(CouponRules.evaluate(percent(10, expires = now - 1), 300_000, now) is CouponResult.Rejected)
        assertTrue(CouponRules.evaluate(percent(10, expires = now), 300_000, now) is CouponResult.Applied)
    }

    @Test
    fun `rejects orders below the minimum and says how much is missing`() {
        val result = CouponRules.evaluate(percent(10, min = 50_000), 30_000, now)
        assertEquals("Add ₹200.00 more to use this coupon", (result as CouponResult.Rejected).reason)
    }

    @Test
    fun `percent is clamped to 0 to 100`() {
        assertEquals(10_000, discount(CouponRules.evaluate(percent(250), 10_000, now)))
    }
}
