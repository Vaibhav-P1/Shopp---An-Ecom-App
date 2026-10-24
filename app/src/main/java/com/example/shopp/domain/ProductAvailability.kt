package com.example.shopp.domain

import com.example.shopp.model.ProductModel

/** A product can be bought when its seller has not hidden it and, if stock is tracked, units remain. */
val ProductModel.isAvailable: Boolean
    get() = active && (stock == null || stock > 0)

/** Shown instead of the buy button when a product cannot be bought. */
val ProductModel.unavailableLabel: String
    get() = if (!active) "Unavailable" else "Out of stock"

/** "Only 3 left" for low stock, or null when there is plenty or stock is not tracked. */
val ProductModel.lowStockLabel: String?
    get() = stock?.takeIf { it in 1..LOW_STOCK_THRESHOLD }?.let { "Only $it left" }

const val LOW_STOCK_THRESHOLD = 5L
