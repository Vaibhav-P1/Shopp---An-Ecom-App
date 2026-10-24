package com.example.shopp.domain

/** Per-field problems of the seller sign-up form; a null field is valid. */
data class SellerFormErrors(val shopName: String? = null, val phone: String? = null) {
    val hasErrors: Boolean get() = shopName != null || phone != null
}

object SellerValidator {

    private const val MIN_SHOP_NAME = 2
    private const val MAX_SHOP_NAME = 40

    fun validate(shopName: String, phone: String): SellerFormErrors = SellerFormErrors(
        shopName = when (shopName.trim().length) {
            in MIN_SHOP_NAME..MAX_SHOP_NAME -> null
            else -> "Shop name must be $MIN_SHOP_NAME to $MAX_SHOP_NAME characters"
        },
        phone = if (AddressValidator.isValidPhone(phone)) null else "Enter a 10-digit mobile number"
    )
}
