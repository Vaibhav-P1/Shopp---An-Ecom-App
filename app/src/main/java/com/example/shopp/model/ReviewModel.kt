package com.example.shopp.model

import com.google.firebase.Timestamp

/**
 * A customer review, stored at `data/stock/products/{productId}/reviews/{userId}`. The document id is the
 * author's uid, which is how Firestore rules limit each user to one review per product.
 */
data class ReviewModel(
    val userId: String = "",
    val userName: String = "",
    val orderId: String = "",
    val rating: Long = 0,
    val text: String = "",
    val updatedAt: Timestamp? = null
)
