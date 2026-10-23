package com.example.shopp.model

import com.google.firebase.Timestamp

/** Snapshot of a product at the time it was ordered, so later price changes never rewrite history. */
data class OrderLineModel(
    val productId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val unitPricePaise: Long = 0,
    val quantity: Long = 0
)

data class OrderModel(
    val id: String = "",
    val date: Timestamp = Timestamp.now(),
    val userId: String = "",
    val items: Map<String, Long> = mapOf(), // productId -> quantity; kept for orders placed before line snapshots
    val status: String = "",
    val address: String = "",
    val lines: List<OrderLineModel> = emptyList(),
    val subtotalPaise: Long = 0,
    val discountPaise: Long = 0,
    val taxPaise: Long = 0,
    val totalPaise: Long = 0,
    val paymentMethod: String = "",
    val paymentId: String = ""
)
