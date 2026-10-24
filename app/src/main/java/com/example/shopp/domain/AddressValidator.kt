package com.example.shopp.domain

/** Per-field problems of an address form; a null field is valid. */
data class AddressErrors(
    val label: String? = null,
    val recipient: String? = null,
    val phone: String? = null,
    val street: String? = null,
    val city: String? = null,
    val pincode: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(label, recipient, phone, street, city, pincode).any { it != null }
}

object AddressValidator {

    fun validate(
        label: String,
        recipient: String,
        phone: String,
        street: String,
        city: String,
        pincode: String
    ): AddressErrors = AddressErrors(
        label = if (label.isBlank()) "Give this address a name, e.g. Home" else null,
        recipient = if (recipient.isBlank()) "Enter the recipient name" else null,
        phone = if (isValidPhone(phone)) null else "Enter a 10-digit mobile number",
        street = if (street.trim().length >= 5) null else "Enter house number and street",
        city = if (city.isBlank()) "Enter the city" else null,
        pincode = if (PINCODE.matches(pincode.trim())) null else "Enter a 6-digit pincode"
    )

    fun isValidPhone(phone: String): Boolean = PHONE.matches(phone.trim())

    // Indian mobile numbers start with 6-9; PIN codes are six digits and never start with 0
    private val PHONE = Regex("[6-9]\\d{9}")
    private val PINCODE = Regex("[1-9]\\d{5}")
}
