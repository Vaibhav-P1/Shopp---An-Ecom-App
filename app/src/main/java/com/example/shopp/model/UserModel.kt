package com.example.shopp.model

data class UserModel(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val cartItems: Map<String, Long> = emptyMap(), // Key: Product ID, Value: Quantity
    val address: String = "" // Added for delivery processing
)
