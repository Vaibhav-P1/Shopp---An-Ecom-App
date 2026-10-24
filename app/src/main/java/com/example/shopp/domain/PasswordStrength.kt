package com.example.shopp.domain

/** A rough password rating for the signup meter. It guides the user; the real rules live in the auth validator. */
enum class PasswordStrength(val bars: Int, val label: String) {
    EMPTY(0, ""),
    WEAK(1, "Weak, use 8+ characters"),
    FAIR(2, "Fair, add numbers or symbols"),
    GOOD(3, "Good"),
    STRONG(4, "Strong");

    companion object {
        fun of(password: String): PasswordStrength {
            if (password.isEmpty()) return EMPTY
            var score = 0
            if (password.length >= 8) score++
            if (password.length >= 12) score++
            if (password.any { it.isLowerCase() } && password.any { it.isUpperCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++
            return when {
                password.length < 8 -> WEAK
                score <= 2 -> FAIR
                score == 3 -> GOOD
                else -> STRONG
            }
        }
    }
}
