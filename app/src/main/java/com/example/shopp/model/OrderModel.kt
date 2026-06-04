package com.example.shopp.model

import com.google.firebase.Timestamp

data class OrderModel(
    val id: String = "",
    val date: Timestamp = Timestamp.now(),
    val userId: String = "",
    val items: Map<String, Long> = mapOf(), // Duplicates structural maps from checkout states
    val status: String = "",
    val address: String = ""
)
