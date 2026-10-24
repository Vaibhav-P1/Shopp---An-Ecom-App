package com.example.shopp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.SecondaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.StatusPill
import com.example.shopp.components.VerticalOrderTimeline
import com.example.shopp.domain.Money
import com.example.shopp.model.OrderLineModel
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus
import com.example.shopp.ui.formatDate
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.OrderDetailsViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun OrderDetailsPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCancelling by viewModel.isCancelling.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { context.showToast(it) }
    }

    Column(modifier.fillMaxSize()) {
        when (val state = uiState) {
            UiState.Loading -> {
                DetailsHeader(title = "Order", subtitle = null, onBack = onBack)
                LoadingView()
            }
            is UiState.Error -> {
                DetailsHeader(title = "Order", subtitle = null, onBack = onBack)
                ErrorView(state.message, onRetry = viewModel::retry)
            }
            is UiState.Success -> {
                DetailsHeader(
                    title = state.data.id,
                    subtitle = "Placed ${state.data.date.formatDate()}",
                    onBack = onBack
                )
                OrderDetailsContent(
                    order = state.data,
                    isCancelling = isCancelling,
                    onCancel = viewModel::cancelOrder
                )
            }
        }
    }
}

@Composable
private fun DetailsHeader(title: String, subtitle: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(
    order: OrderModel,
    isCancelling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmCancel by remember { mutableStateOf(false) }
    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("Cancel this order?") },
            text = { Text("Order ${order.id} will be cancelled. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    onCancel()
                }) { Text("Cancel order") }
            },
            dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("Keep order") } }
        )
    }
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (order.status == OrderStatus.CANCELLED) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatusPill(order.status)
                            Text(
                                text = order.cancelledAt?.let { "on ${it.formatDate()}" } ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                    } else {
                        VerticalOrderTimeline(
                            status = order.status,
                            captions = mapOf(OrderStatus.ORDERED to order.date.formatDate())
                        )
                    }
                }
            }
        }

        item {
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (order.lines.isEmpty()) {
                        Text(
                            "${order.items.values.sum()} item(s). Item details are not available for this order.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        order.lines.forEach { OrderLineRow(it) }
                    }
                    if (order.totalPaise > 0) {
                        HorizontalDivider(color = colors.surfaceVariant)
                        AmountRow("Subtotal", Money.format(order.subtotalPaise))
                        if (order.discountPaise > 0) {
                            AmountRow(
                                if (order.couponCode.isBlank()) "Discount" else "Discount (${order.couponCode})",
                                "-${Money.format(order.discountPaise)}",
                                highlight = true
                            )
                        }
                        AmountRow("Tax", Money.format(order.taxPaise))
                        AmountRow("Total", Money.format(order.totalPaise), bold = true)
                    }
                    AmountRow("Payment", paymentLabel(order))
                }
            }
        }

        item {
            ShoppCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Delivering to", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            order.address.ifBlank { "Not available" },
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5f),
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (order.status != OrderStatus.CANCELLED && order.status != OrderStatus.DELIVERED) {
            item {
                if (order.status == OrderStatus.ORDERED) {
                    SecondaryButton(
                        text = if (isCancelling) "Cancelling..." else "Cancel order",
                        onClick = { confirmCancel = true },
                        enabled = !isCancelling
                    )
                } else {
                    SecondaryButton(text = "Cancel unavailable after packing", onClick = {}, enabled = false)
                }
            }
        }
        item { Box(Modifier.height(Spacing.lg)) }
    }
}

@Composable
private fun OrderLineRow(line: OrderLineModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AsyncImage(
            model = line.imageUrl.ifBlank { null },
            contentDescription = line.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        )
        Text(
            text = "${line.title}  ×${line.quantity}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
        Text(
            Money.format(line.unitPricePaise * line.quantity),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun AmountRow(label: String, value: String, bold: Boolean = false, highlight: Boolean = false) {
    val style = if (bold) {
        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.bodySmall.copy(fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal)
    }
    val color = when {
        highlight -> MaterialTheme.colorScheme.primary
        bold -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style, color = color)
        Text(value, style = style, color = color)
    }
}

private fun paymentLabel(order: OrderModel): String = when (order.paymentMethod) {
    "COD" -> "Cash on delivery"
    "ONLINE" -> "Paid online"
    else -> order.paymentMethod.ifBlank { "Not available" }
}
