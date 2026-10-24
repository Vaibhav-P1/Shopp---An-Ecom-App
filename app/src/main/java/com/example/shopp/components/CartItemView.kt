package com.example.shopp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.domain.lowStockLabel
import com.example.shopp.viewmodel.CartLine

@Composable
fun CartItemView(
    line: CartLine,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val product = line.product
    val colors = MaterialTheme.colorScheme

    ShoppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(enabled = product != null, onClick = onClick)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = product?.images?.firstOrNull(),
                contentDescription = product?.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainer)
            )

            Column(
                modifier = Modifier.weight(1f).height(88.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = product?.title ?: "Product no longer available",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        product?.lowStockLabel?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.secondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove from cart",
                        tint = colors.outline,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onRemove)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (product != null) {
                        Text(
                            text = "₹${product.actualPrice}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    } else {
                        Box(Modifier)
                    }
                    QuantityStepper(
                        quantity = line.quantity,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement,
                        canIncrement = product != null && (product.stock == null || line.quantity < product.stock)
                    )
                }
            }
        }
    }
}

/** Pill with a minus button, the count and a plus button. */
@Composable
fun QuantityStepper(
    quantity: Long,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    canIncrement: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceContainerLow)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StepperButton(Icons.Default.Remove, "Decrease quantity", enabled = true, onClick = onDecrement)
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 14.dp)
        )
        StepperButton(Icons.Default.Add, "Increase quantity", enabled = canIncrement, onClick = onIncrement)
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
