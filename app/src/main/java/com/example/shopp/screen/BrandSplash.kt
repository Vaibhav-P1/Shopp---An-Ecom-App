package com.example.shopp.screen

import androidx.compose.foundation.background
import com.example.shopp.ui.theme.isAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.components.BrandLogo
import com.example.shopp.components.BrandWordmark
import com.example.shopp.ui.theme.CoralContainerLight
import com.example.shopp.ui.theme.CoralDark
import com.example.shopp.ui.theme.EmeraldDark
import com.example.shopp.ui.theme.SurfaceDark

const val TAGLINE = "Shop smart. Smile more."

/**
 * Brand screen shown right after the system splash, which uses the same colours so the hand-off
 * is seamless: emerald gradient with the logo tile in light mode, near-black in dark mode.
 */
@Composable
fun BrandSplash(modifier: Modifier = Modifier) {
    val dark = isAppDarkTheme()
    val background = if (dark) {
        Brush.verticalGradient(listOf(SurfaceDark, SurfaceDark))
    } else {
        Brush.linearGradient(listOf(Color(0xFF12A878), Color(0xFF0A8F63), Color(0xFF066B4A)))
    }
    val textColor = if (dark) MaterialTheme.colorScheme.onBackground else Color.White
    Column(
        modifier = modifier.fillMaxSize().background(background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandLogo(size = 132.dp, elevated = true)
        Spacer(Modifier.height(20.dp))
        BrandWordmark(
            fontSize = 48.sp,
            color = textColor,
            dotColor = if (dark) CoralDark else CoralContainerLight
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = TAGLINE,
            color = textColor.copy(alpha = 0.92f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
