package com.example.shopp.model

/**
 * A saved delivery address, stored at `users/{uid}/addresses/{id}`. [primary] marks the one used at checkout
 * (named so Firestore's bean mapping does not mangle an `isDefault` getter into a "default" field).
 */
data class AddressModel(
    val id: String = "",
    val label: String = "",
    val recipient: String = "",
    val phone: String = "",
    val street: String = "",
    val city: String = "",
    val pincode: String = "",
    val primary: Boolean = false
) {
    /** Multi-line text used on screens and copied onto orders. */
    val formatted: String
        get() = "$recipient, $phone\n$street, $city - $pincode"
}
