package com.example.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceCalculatorTest {

    @Test
    fun `zero subtotal gives zero everything`() {
        val breakdown = PriceCalculator.calculate(0)
        assertEquals(0, breakdown.discountPaise)
        assertEquals(0, breakdown.taxPaise)
        assertEquals(0, breakdown.totalPaise)
    }

    @Test
    fun `without a coupon there is no discount and 13 percent tax`() {
        // Rs 3000.00
        val breakdown = PriceCalculator.calculate(300_000)
        assertEquals(0, breakdown.discountPaise)
        assertEquals(39_000, breakdown.taxPaise)
        assertEquals(339_000, breakdown.totalPaise)
    }

    @Test
    fun `tax is charged on the discounted amount`() {
        // Rs 3000 less Rs 300 -> tax on Rs 2700
        val breakdown = PriceCalculator.calculate(300_000, discountPaise = 30_000)
        assertEquals(30_000, breakdown.discountPaise)
        assertEquals(35_100, breakdown.taxPaise)
        assertEquals(305_100, breakdown.totalPaise)
    }

    @Test
    fun `discount can never exceed the subtotal`() {
        val breakdown = PriceCalculator.calculate(10_000, discountPaise = 50_000)
        assertEquals(10_000, breakdown.discountPaise)
        assertEquals(0, breakdown.taxPaise)
        assertEquals(0, breakdown.totalPaise)
    }

    @Test
    fun `rounds fractional paise half up`() {
        // 5 paise: tax 0.65 -> 1; 15 paise: tax 1.95 -> 2; 1 paisa: tax 0.13 -> 0
        assertEquals(1, PriceCalculator.calculate(5).taxPaise)
        assertEquals(2, PriceCalculator.calculate(15).taxPaise)
        assertEquals(0, PriceCalculator.calculate(1).taxPaise)
    }

    @Test
    fun `total equals subtotal minus discount plus tax`() {
        val breakdown = PriceCalculator.calculate(123_457, discountPaise = 1_234)
        assertEquals(
            breakdown.subtotalPaise - breakdown.discountPaise + breakdown.taxPaise,
            breakdown.totalPaise
        )
    }

    @Test
    fun `parsePaise handles whole and decimal rupees`() {
        assertEquals(300_000L, Money.parsePaise("3000"))
        assertEquals(9_950L, Money.parsePaise("99.50"))
        assertEquals(9_951L, Money.parsePaise(" 99.505 "))
    }

    @Test
    fun `parsePaise rejects invalid or negative input`() {
        assertNull(Money.parsePaise(""))
        assertNull(Money.parsePaise("abc"))
        assertNull(Money.parsePaise("-5"))
    }

    @Test
    fun `format pads paise to two digits`() {
        assertEquals("₹3000.00", Money.format(300_000))
        assertEquals("₹0.05", Money.format(5))
        assertEquals("₹99.50", Money.format(9_950))
    }
}
