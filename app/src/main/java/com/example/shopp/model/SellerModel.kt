package com.example.shopp.model

import com.google.firebase.Timestamp

/** A seller profile, stored at `sellers/{uid}`. Having this document plus the "seller" role makes a user a seller. */
data class SellerModel(
    val shopName: String = "",
    val phone: String = "",
    val status: String = SellerStatus.APPROVED,
    val createdAt: Timestamp? = null
)

object SellerStatus {
    const val APPROVED = "approved"
}
