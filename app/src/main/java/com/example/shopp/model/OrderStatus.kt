package com.example.shopp.model

/** Stored values of [OrderModel.status]. Orders move ORDERED -> PACKED -> SHIPPED -> DELIVERED, or end CANCELLED. */
object OrderStatus {
    const val ORDERED = "ORDERED"
    const val PACKED = "PACKED"
    const val SHIPPED = "SHIPPED"
    const val DELIVERED = "DELIVERED"
    const val CANCELLED = "CANCELLED"

    /** The happy-path steps in order, used for the progress timeline. */
    val timeline = listOf(ORDERED, PACKED, SHIPPED, DELIVERED)

    fun label(status: String): String = when (status) {
        ORDERED -> "Placed"
        else -> status.lowercase().replaceFirstChar { it.uppercase() }
    }
}
