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

    /** The step after [status] on the happy path, or null once delivered, cancelled or unknown. */
    fun next(status: String): String? = timeline.getOrNull(timeline.indexOf(status) + 1).takeIf { status in timeline }

    /** Button text a seller sees to move an order out of [status], e.g. "Mark as packed". */
    fun actionLabel(status: String): String? = next(status)?.let { "Mark as ${label(it).lowercase()}" }

    fun label(status: String): String = when (status) {
        ORDERED -> "Placed"
        else -> status.lowercase().replaceFirstChar { it.uppercase() }
    }
}
