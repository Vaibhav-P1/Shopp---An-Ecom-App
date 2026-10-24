package com.example.shopp.model

data class ProductModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val actualPrice: String = "",
    val images: List<String> = emptyList(),
    val otherDetails: Map<String, String> = mapOf(), // Added for dynamic specs
    /** Owner of a seller-listed product; blank for the original catalogue. */
    val sellerId: String = "",
    /** Units in stock; null means stock is not tracked for this product. */
    val stock: Long? = null,
    /** Sellers can hide a product without deleting it. */
    val active: Boolean = true
)
