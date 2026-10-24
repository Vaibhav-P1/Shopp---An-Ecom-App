package com.example.shopp.domain

import com.example.shopp.model.ProductModel

enum class SortOrder(val label: String) {
    Relevance("Relevance"),
    PriceLowToHigh("Price: low to high"),
    PriceHighToLow("Price: high to low"),
    NameAToZ("Name: A to Z")
}

/** User-chosen narrowing of search results. A null price bound means unbounded on that side. */
data class SearchFilters(
    val categoryIds: Set<String> = emptySet(),
    val minPricePaise: Long? = null,
    val maxPricePaise: Long? = null,
    val sort: SortOrder = SortOrder.Relevance
) {
    /** Number of filters that differ from the defaults (shown as a badge on the filter button). */
    val activeCount: Int
        get() = (if (categoryIds.isNotEmpty()) 1 else 0) +
            (if (minPricePaise != null || maxPricePaise != null) 1 else 0) +
            (if (sort != SortOrder.Relevance) 1 else 0)
}

/** The selling price of a product in paise, or null if the catalogue entry has no valid price. */
val ProductModel.sellingPricePaise: Long?
    get() = Money.parsePaise(actualPrice) ?: Money.parsePaise(price)

/** Client-side catalogue search: pure so it can be unit tested without Firestore. */
object ProductSearch {

    /**
     * Products matching every word of [query] (in title, description or category) and all [filters].
     * A blank query matches everything. With [SortOrder.Relevance], title matches rank first.
     */
    fun search(products: List<ProductModel>, query: String, filters: SearchFilters): List<ProductModel> {
        val words = query.lowercase().split(WHITESPACE).filter { it.isNotEmpty() }

        val matched = products
            .filter { product -> matchesFilters(product, filters) }
            .mapNotNull { product ->
                val score = relevance(product, words)
                if (score == null) null else product to score
            }

        return when (filters.sort) {
            SortOrder.Relevance -> matched.sortedWith(
                compareByDescending<Pair<ProductModel, Int>> { it.second }.thenBy { it.first.title.lowercase() }
            )
            SortOrder.PriceLowToHigh -> matched.sortedBy { it.first.sellingPricePaise ?: Long.MAX_VALUE }
            SortOrder.PriceHighToLow -> matched.sortedByDescending { it.first.sellingPricePaise ?: Long.MIN_VALUE }
            SortOrder.NameAToZ -> matched.sortedBy { it.first.title.lowercase() }
        }.map { it.first }
    }

    /** Highest price in the catalogue, used as the upper end of the price slider. */
    fun maxPricePaise(products: List<ProductModel>): Long =
        products.mapNotNull { it.sellingPricePaise }.maxOrNull() ?: 0L

    private fun matchesFilters(product: ProductModel, filters: SearchFilters): Boolean {
        if (filters.categoryIds.isNotEmpty() && product.category !in filters.categoryIds) return false
        if (filters.minPricePaise != null || filters.maxPricePaise != null) {
            val price = product.sellingPricePaise ?: return false
            if (filters.minPricePaise != null && price < filters.minPricePaise) return false
            if (filters.maxPricePaise != null && price > filters.maxPricePaise) return false
        }
        return true
    }

    /** Null when some query word is missing; otherwise a score where higher means a better match. */
    private fun relevance(product: ProductModel, words: List<String>): Int? {
        if (words.isEmpty()) return 0
        val title = product.title.lowercase()
        val description = product.description.lowercase()
        val category = product.category.lowercase()

        var score = 0
        for (word in words) {
            score += when {
                title.startsWith(word) -> 4
                title.split(WHITESPACE).any { it.startsWith(word) } -> 3
                title.contains(word) -> 2
                category.contains(word) || description.contains(word) -> 1
                else -> return null
            }
        }
        return score
    }

    private val WHITESPACE = Regex("\\s+")
}
