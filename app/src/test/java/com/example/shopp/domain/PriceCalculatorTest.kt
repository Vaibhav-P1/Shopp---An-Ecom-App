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
    fun `applies 10 percent discount and 13 percent tax`() {
        // Rs 3000.00
        val breakdown = PriceCalculator.calculate(300_000)
        assertEquals(30_000, breakdown.discountPaise)
        assertEquals(39_000, breakdown.taxPaise)
        assertEquals(309_000, breakdown.totalPaise)
    }

    @Test
    fun `rounds fractional paise half up`() {
        // 1 paisa: discount 0.1 -> 0, tax 0.13 -> 0
        assertEquals(0, PriceCalculator.calculate(1).discountPaise)
        // 5 paise: discount 0.5 -> 1, tax 0.65 -> 1
        val five = PriceCalculator.calculate(5)
        assertEquals(1, five.discountPaise)
        assertEquals(1, five.taxPaise)
        // 15 paise: discount 1.5 -> 2, tax 1.95 -> 2
        val fifteen = PriceCalculator.calculate(15)
        assertEquals(2, fifteen.discountPaise)
        assertEquals(2, fifteen.taxPaise)
    }

    @Test
    fun `total equals subtotal minus discount plus tax`() {
        val breakdown = PriceCalculator.calculate(123_457)
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
