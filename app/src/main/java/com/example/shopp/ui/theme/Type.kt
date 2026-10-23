package com.example.shopp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.shopp.R

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private fun TextStyle.poppins() = copy(fontFamily = Poppins)

/** The Material 3 type scale with Poppins applied to every style. */
val Typography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.poppins(),
        displayMedium = base.displayMedium.poppins(),
        displaySmall = base.displaySmall.poppins(),
        headlineLarge = base.headlineLarge.poppins(),
        headlineMedium = base.headlineMedium.poppins(),
        headlineSmall = base.headlineSmall.poppins(),
        titleLarge = base.titleLarge.poppins().copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.poppins().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.poppins(),
        bodyLarge = base.bodyLarge.poppins(),
        bodyMedium = base.bodyMedium.poppins(),
        bodySmall = base.bodySmall.poppins(),
        labelLarge = base.labelLarge.poppins(),
        labelMedium = base.labelMedium.poppins(),
        labelSmall = base.labelSmall.poppins()
    )
}
