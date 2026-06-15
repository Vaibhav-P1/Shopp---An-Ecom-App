package com.example.shopp.model

data class OfferModel(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val discount: String = "",
    val active: Boolean = true,
    val priority: Int = 0
)