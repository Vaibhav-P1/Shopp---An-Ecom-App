package com.example.shopp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CardShape = RoundedCornerShape(22.dp)

/**
 * Bordered card on the lightest surface with the app's 22dp shape. Pass [onClick] to make it
 * tappable; [container] swaps the fill (for example a tonal summary card).
 */
@Composable
fun ShoppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = container)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = CardShape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = CardShape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    }
}
