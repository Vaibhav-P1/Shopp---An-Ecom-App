package com.example.shopp.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.shopp.R

/** The Shopp bag-with-a-smile logo on a white rounded tile. */
@Composable
fun BrandLogo(size: Dp, modifier: Modifier = Modifier, elevated: Boolean = false) {
    val shape = RoundedCornerShape(size * 0.29f)
    Box(
        modifier = modifier
            .size(size)
            .then(if (elevated) Modifier.shadow(18.dp, shape) else Modifier)
            .background(Color.White, shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_mark),
            contentDescription = "Shopp logo",
            modifier = Modifier.size(size * 0.7f)
        )
    }
}

/** "shopp." wordmark; the dot takes [dotColor]. */
@Composable
fun BrandWordmark(
    fontSize: TextUnit,
    color: Color,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            append("shopp")
            withStyle(SpanStyle(color = dotColor)) { append(".") }
        },
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.displaySmall.copy(
            fontSize = fontSize,
            lineHeight = fontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.04f).em
        )
    )
}
