package com.example.shopp.domain

import com.example.shopp.model.ProductModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductDraftTest {

    private val valid = ProductDraft(
        title = "Cotton Shirt",
        description = "Soft",
        category = "fashion",
        price = "999",
        actualPrice = "799.50",
        stock = "12",
        images = listOf("https://example.com/a.jpg")
    )

    @Test
    fun `a complete draft is valid`() {
        assertFalse(ProductDraftValidator.validate(valid).hasErrors)
    }

    @Test
    fun `required fields are reported`() {
        val errors = ProductDraftValidator.validate(ProductDraft())
        assertNotNull(errors.title)
        assertNotNull(errors.category)
        assertNotNull(errors.price)
        assertNotNull(errors.actualPrice)
        assertNotNull(errors.stock)
        assertNotNull(errors.images)
    }

    @Test
    fun `selling price cannot exceed the list price`() {
        assertNotNull(ProductDraftValidator.validate(valid.copy(actualPrice = "1000")).actualPrice)
        assertNull(ProductDraftValidator.validate(valid.copy(actualPrice = "999")).actualPrice)
    }

    @Test
    fun `stock must be a whole number of zero or more`() {
        assertNull(ProductDraftValidator.validate(valid.copy(stock = "0")).stock)
        assertNotNull(ProductDraftValidator.validate(valid.copy(stock = "-1")).stock)
        assertNotNull(ProductDraftValidator.validate(valid.copy(stock = "2.5")).stock)
    }

    @Test
    fun `availability follows active flag and tracked stock`() {
        assertTrue(ProductModel(stock = null).isAvailable)
        assertTrue(ProductModel(stock = 3).isAvailable)
        assertFalse(ProductModel(stock = 0).isAvailable)
        assertFalse(ProductModel(active = false, stock = 5).isAvailable)
        assertEquals("Unavailable", ProductModel(active = false).unavailableLabel)
        assertEquals("Out of stock", ProductModel(stock = 0).unavailableLabel)
    }

    @Test
    fun `low stock label only for one to five units`() {
        assertEquals("Only 2 left", ProductModel(stock = 2).lowStockLabel)
        assertNull(ProductModel(stock = 0).lowStockLabel)
        assertNull(ProductModel(stock = 6).lowStockLabel)
        assertNull(ProductModel(stock = null).lowStockLabel)
    }
}
