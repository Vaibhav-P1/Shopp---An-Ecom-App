package com.example.shopp.domain

import java.math.RoundingMode
import java.util.Locale

/** Money is handled as whole paise (`Long`) everywhere; floating point is never used for amounts. */
object Money {

    /** Parses a rupee amount such as "3000" or "99.50" into paise, or null if it isn't a valid non-negative amount. */
    fun parsePaise(rupees: String): Long? =
        rupees.trim().toBigDecimalOrNull()
            ?.takeIf { it.signum() >= 0 }
            ?.movePointRight(2)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.toLong()

    /** Formats paise for display, e.g. 300050 -> "₹3000.50". */
    fun format(paise: Long): String =
        String.format(Locale.US, "₹%d.%02d", paise / 100, paise % 100)
}
