package com.example.shopp

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. Arguments are plain properties and are read back with `toRoute`. */
@Serializable data object Auth
@Serializable data object Login
@Serializable data object Signup
@Serializable data object Home
@Serializable data object Checkout
@Serializable data object Orders
@Serializable data object Seller
@Serializable data class CategoryProducts(val categoryId: String)
@Serializable data class ProductDetails(val productId: String)
@Serializable data class OrderDetails(val orderId: String)
