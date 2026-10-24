package com.example.shopp.domain

import com.example.shopp.model.ProductModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductSearchTest {

    private fun product(id: String, title: String, category: String = "shoes", price: String = "100", description: String = "") =
        ProductModel(id = id, title = title, category = category, price = price, actualPrice = price, description = description)

    private val catalogue = listOf(
        product("1", "Nike Air Zoom", "shoes", "5000"),
        product("2", "Adidas Running Shoe", "shoes", "3000", description = "light and fast"),
        product("3", "Cotton T-Shirt", "fashion", "800"),
        product("4", "Gaming Headphones", "electronics", "2500", description = "for shoe lovers"),
        product("5", "Broken Price Item", "fashion", "not-a-price")
    )

    private fun ids(query: String, filters: SearchFilters = SearchFilters()) =
        ProductSearch.search(catalogue, query, filters).map { it.id }

    @Test
    fun `blank query returns everything`() {
        assertEquals(5, ids("   ").size)
    }

    @Test
    fun `matches every word case-insensitively`() {
        assertEquals(listOf("1"), ids("NIKE zoom"))
    }

    @Test
    fun `title matches rank above description matches`() {
        // "shoe" starts a title word for 2; 1 (category) and 4 (description) tie and fall back to title order
        assertEquals(listOf("2", "4", "1"), ids("shoe"))
    }

    @Test
    fun `category filter keeps only chosen categories`() {
        assertEquals(setOf("3", "5"), ids("", SearchFilters(categoryIds = setOf("fashion"))).toSet())
    }

    @Test
    fun `price range is inclusive and drops products without a valid price`() {
        val filters = SearchFilters(minPricePaise = 80_000, maxPricePaise = 300_000)
        assertEquals(setOf("2", "3", "4"), ids("", filters).toSet())
    }

    @Test
    fun `sorts by price both ways with invalid prices last`() {
        assertEquals(listOf("3", "4", "2", "1", "5"), ids("", SearchFilters(sort = SortOrder.PriceLowToHigh)))
        assertEquals(listOf("1", "2", "4", "3"), ids("", SearchFilters(sort = SortOrder.PriceHighToLow)).take(4))
    }

    @Test
    fun `sorts by name`() {
        assertEquals("2", ids("", SearchFilters(sort = SortOrder.NameAToZ)).first())
    }

    @Test
    fun `counts active filters`() {
        assertEquals(0, SearchFilters().activeCount)
        assertEquals(3, SearchFilters(setOf("a"), 1L, null, SortOrder.NameAToZ).activeCount)
    }

    @Test
    fun `max price ignores invalid entries`() {
        assertEquals(500_000, ProductSearch.maxPricePaise(catalogue))
    }
}
