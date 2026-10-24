package com.example.shopp.components

import androidx.compose.foundation.background
import com.example.shopp.ui.theme.isAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.domain.Money
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus
import com.example.shopp.ui.formatDate

@Composable
fun OrderView(orderItem: OrderModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val cancelled = orderItem.status == OrderStatus.CANCELLED
    val itemCount = if (orderItem.lines.isNotEmpty()) orderItem.lines.sumOf { it.quantity.toInt() } else orderItem.items.values.sum().toInt()
    val first = orderItem.lines.firstOrNull()
    val summary = when {
        first == null -> "$itemCount ${if (itemCount == 1) "item" else "items"}"
        orderItem.lines.size > 1 -> "${first.title} + ${orderItem.lines.size - 1} more"
        else -> first.title
    }

    ShoppCard(onClick = onClick, modifier = modifier.fillMaxWidth().alpha(if (cancelled) 0.85f else 1f)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(orderItem.id, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                StatusPill(orderItem.status)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                orderItem.lines.take(2).forEach { line ->
                    AsyncImage(
                        model = line.imageUrl.ifBlank { null },
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.surfaceContainer)
                    )
                }
                Column(Modifier.weight(1f).height(52.dp), verticalArrangement = Arrangement.Center) {
                    Text(summary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), maxLines = 1)
                    Text(
                        "${orderItem.date.formatDate()} · $itemCount ${if (itemCount == 1) "item" else "items"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            if (orderItem.totalPaise > 0) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = Money.format(orderItem.totalPaise),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (cancelled) colors.outline else colors.onSurface
                    )
                    Text(paymentName(orderItem.paymentMethod), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
            }
            if (!cancelled) StatusSegments(orderItem.status)
        }
    }
}

/** Four thin bars that fill up as the order moves through the delivery steps. */
@Composable
private fun StatusSegments(status: String) {
    val reached = OrderStatus.timeline.indexOf(status)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        OrderStatus.timeline.forEachIndexed { index, _ ->
            val color = when {
                index < reached -> MaterialTheme.colorScheme.primary
                index == reached && status == OrderStatus.DELIVERED -> MaterialTheme.colorScheme.primary
                index == reached -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

/** Rounded label coloured by order status. */
@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (container, content) = statusColors(status)
    Text(
        text = statusLabel(status),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = content,
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun statusColors(status: String): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    val dark = isAppDarkTheme()
    return when (status) {
        OrderStatus.DELIVERED -> colors.primaryContainer to colors.onPrimaryContainer
        OrderStatus.CANCELLED -> colors.secondaryContainer to colors.onSecondaryContainer
        OrderStatus.SHIPPED -> if (dark) Color(0xFF5B4300) to Color(0xFFFFE9B8) else Color(0xFFFFE9B8) to Color(0xFF5B4300)
        else -> colors.tertiaryContainer to colors.onTertiaryContainer
    }
}

private fun paymentName(method: String): String = when (method) {
    "COD" -> "Cash on delivery"
    "ONLINE" -> "Paid online"
    else -> method
}

/** User-facing name for a stored order status. */
fun statusLabel(status: String): String = OrderStatus.label(status)
