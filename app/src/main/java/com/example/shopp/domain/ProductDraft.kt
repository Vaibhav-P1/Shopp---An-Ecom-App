package com.example.shopp.domain

/** What a seller types into the product form, before it is validated and saved. */
data class ProductDraft(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    /** List price (MRP) in rupees, as typed. */
    val price: String = "",
    /** Selling price in rupees, as typed. */
    val actualPrice: String = "",
    val stock: String = "",
    val images: List<String> = emptyList(),
    val active: Boolean = true
)

data class ProductDraftErrors(
    val title: String? = null,
    val category: String? = null,
    val price: String? = null,
    val actualPrice: String? = null,
    val stock: String? = null,
    val images: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(title, category, price, actualPrice, stock, images).any { it != null }
}

object ProductDraftValidator {

    const val MAX_IMAGES = 6
    private const val MIN_TITLE = 3
    private const val MAX_TITLE = 80

    fun validate(draft: ProductDraft): ProductDraftErrors {
        val mrp = Money.parsePaise(draft.price)
        val selling = Money.parsePaise(draft.actualPrice)
        return ProductDraftErrors(
            title = if (draft.title.trim().length in MIN_TITLE..MAX_TITLE) null else "Title must be $MIN_TITLE to $MAX_TITLE characters",
            category = if (draft.category.isBlank()) "Choose a category" else null,
            price = if (mrp == null || mrp <= 0) "Enter the list price in rupees" else null,
            actualPrice = when {
                selling == null || selling <= 0 -> "Enter the selling price in rupees"
                mrp != null && selling > mrp -> "Selling price cannot be above the list price"
                else -> null
            },
            stock = if (draft.stock.trim().toLongOrNull()?.let { it >= 0 } == true) null else "Enter the units in stock (0 or more)",
            images = if (draft.images.isEmpty()) "Add at least one image" else null
        )
    }
}
