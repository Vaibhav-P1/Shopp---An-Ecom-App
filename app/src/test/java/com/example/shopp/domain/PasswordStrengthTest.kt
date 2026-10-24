package com.example.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordStrengthTest {

    @Test
    fun `empty password has no bars`() {
        assertEquals(PasswordStrength.EMPTY, PasswordStrength.of(""))
    }

    @Test
    fun `short password is weak`() {
        assertEquals(PasswordStrength.WEAK, PasswordStrength.of("Ab1!"))
    }

    @Test
    fun `eight lowercase letters is fair`() {
        assertEquals(PasswordStrength.FAIR, PasswordStrength.of("abcdefgh"))
    }

    @Test
    fun `mixed case with digit is good`() {
        assertEquals(PasswordStrength.GOOD, PasswordStrength.of("Abcdefg1"))
    }

    @Test
    fun `long mixed password with symbol is strong`() {
        assertEquals(PasswordStrength.STRONG, PasswordStrength.of("Abcdefgh1234!"))
    }
}
