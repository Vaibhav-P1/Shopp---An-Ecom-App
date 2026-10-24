package com.example.shopp.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SellerValidatorTest {

    @Test
    fun `valid form has no errors`() {
        assertFalse(SellerValidator.validate("Asha Traders", "9876543210").hasErrors)
    }

    @Test
    fun `shop name is trimmed and length checked`() {
        assertNotNull(SellerValidator.validate("  A ", "9876543210").shopName)
        assertNotNull(SellerValidator.validate("x".repeat(41), "9876543210").shopName)
        assertNull(SellerValidator.validate(" ab ", "9876543210").shopName)
    }

    @Test
    fun `phone must be a valid mobile number`() {
        assertNotNull(SellerValidator.validate("Shop", "12345").phone)
        assertNull(SellerValidator.validate("Shop", "6123456789").phone)
    }
}
