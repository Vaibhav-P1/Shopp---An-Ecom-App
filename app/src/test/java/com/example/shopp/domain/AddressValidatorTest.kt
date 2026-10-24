package com.example.shopp.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressValidatorTest {

    private fun validate(
        label: String = "Home",
        recipient: String = "Asha",
        phone: String = "9876543210",
        street: String = "12 Test Street",
        city: String = "Bengaluru",
        pincode: String = "560001"
    ) = AddressValidator.validate(label, recipient, phone, street, city, pincode)

    @Test
    fun `valid address has no errors`() {
        assertFalse(validate().hasErrors)
    }

    @Test
    fun `blank fields are rejected`() {
        val errors = validate(label = " ", recipient = "", street = "", city = " ")
        assertNotNull(errors.label)
        assertNotNull(errors.recipient)
        assertNotNull(errors.street)
        assertNotNull(errors.city)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun `phone must be ten digits starting 6 to 9`() {
        assertNull(validate(phone = " 9876543210 ").phone)
        assertNotNull(validate(phone = "12345").phone)
        assertNotNull(validate(phone = "5876543210").phone)
        assertNotNull(validate(phone = "98765abcde").phone)
    }

    @Test
    fun `pincode must be six digits not starting with zero`() {
        assertNull(validate(pincode = "110001").pincode)
        assertNotNull(validate(pincode = "056001").pincode)
        assertNotNull(validate(pincode = "5600").pincode)
    }
}
