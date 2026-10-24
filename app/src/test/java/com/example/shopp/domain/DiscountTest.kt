package com.example.shopp.domain

import com.example.shopp.model.ProductModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscountTest {

    private fun product(price: String, actual: String) = ProductModel(price = price, actualPrice = actual)

    @Test
    fun `computes the rounded percentage off`() {
        assertEquals(40, product("4999", "3000").discountPercent)
        assertEquals(25, product("200", "150").discountPercent)
    }

    @Test
    fun `no discount when prices are equal, inverted or invalid`() {
        assertNull(product("100", "100").discountPercent)
        assertNull(product("100", "120").discountPercent)
        assertNull(product("abc", "100").discountPercent)
        assertNull(product("0", "0").discountPercent)
    }
}
