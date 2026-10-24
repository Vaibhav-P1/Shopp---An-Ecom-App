package com.example.shopp.seller

import androidx.compose.foundation.background
import com.example.shopp.ui.theme.isAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.ShoppFilterChip
import com.example.shopp.components.StatusPill
import com.example.shopp.domain.Money
import com.example.shopp.domain.SellerStatsCalculator
import com.example.shopp.domain.isAvailable
import com.example.shopp.domain.linesOf
import com.example.shopp.domain.lowStockLabel
import com.example.shopp.domain.revenueOf
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus
import com.example.shopp.model.ProductModel
import com.example.shopp.ui.formatDate
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.UiState

private enum class OrderGroup(val label: String) {
    New("New"),
    InProgress("In progress"),
    Done("Done");

    fun matches(order: OrderModel): Boolean = when (this) {
        New -> order.status == OrderStatus.ORDERED
        InProgress -> order.status == OrderStatus.PACKED || order.status == OrderStatus.SHIPPED
        Done -> order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED
    }
}

/** Number of orders waiting for the seller to act on, shown in the page header. */
fun newOrderCount(orders: List<OrderModel>): Int = orders.count { it.status == OrderStatus.ORDERED }

/** Orders that contain the seller's products, grouped by stage, each with a button for the next step. */
@Composable
fun SellerOrdersPage(
    state: UiState<List<OrderModel>>,
    sellerId: String,
    updating: Set<String>,
    onRetry: () -> Unit,
    onAdvance: (OrderModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var group by rememberSaveable { mutableStateOf(OrderGroup.New) }
    when (state) {
        UiState.Loading -> ListSkeleton(modifier.padding(Spacing.lg), rows = 4)
        is UiState.Error -> ErrorView(state.message, onRetry = onRetry, modifier = modifier)
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyView("No orders for your products yet", icon = Icons.Outlined.Receipt, modifier = modifier)
        } else {
            Column(modifier.fillMaxSize()) {
                Row(
                    Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OrderGroup.entries.forEach { entry ->
                        val count = state.data.count(entry::matches)
                        ShoppFilterChip(
                            label = if (count > 0 && entry != OrderGroup.Done) "${entry.label} $count" else entry.label,
                            selected = group == entry,
                            onClick = { group = entry }
                        )
                    }
                }
                val shown = state.data.filter(group::matches)
                if (shown.isEmpty()) {
                    EmptyView("No ${group.label.lowercase()} orders", icon = Icons.Outlined.Receipt)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(shown, key = { it.id }) { order ->
                            SellerOrderCard(order, sellerId, order.id in updating, onAdvance = { onAdvance(order) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerOrderCard(order: OrderModel, sellerId: String, isUpdating: Boolean, onAdvance: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ShoppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.id, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                StatusPill(order.status)
            }
            Text(order.date.formatDate(), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            HorizontalDivider(color = colors.surfaceContainer, modifier = Modifier.padding(vertical = 2.dp))

            order.linesOf(sellerId).forEach { line ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AsyncImage(
                        model = line.imageUrl.ifBlank { null },
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceContainer)
                    )
                    Text(
                        text = "${line.title}  ×${line.quantity}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        Money.format(line.unitPricePaise * line.quantity),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Your items", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                Text(Money.format(order.revenueOf(sellerId)), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Column {
                    Text("Ship to", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        order.address,
                        style = MaterialTheme.typography.labelMedium.copy(lineHeight = MaterialTheme.typography.labelMedium.fontSize * 1.4f),
                        color = colors.onSurfaceVariant
                    )
                }
            }

            OrderStatus.actionLabel(order.status)?.let { label ->
                if (order.status == OrderStatus.ORDERED) {
                    PrimaryButton(text = label, onClick = onAdvance, isLoading = isUpdating)
                } else {
                    OutlinedButton(
                        onClick = onAdvance,
                        enabled = !isUpdating,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text(label, style = MaterialTheme.typography.titleSmall) }
                }
            }
        }
    }
}

/** Headline numbers, stock warnings and the newest orders. */
@Composable
fun SellerDashboard(
    shopName: String,
    sellerId: String,
    orders: UiState<List<OrderModel>>,
    products: UiState<List<ProductModel>>,
    modifier: Modifier = Modifier
) {
    val orderList = (orders as? UiState.Success)?.data.orEmpty()
    val productList = (products as? UiState.Success)?.data.orEmpty()
    val stats = SellerStatsCalculator.calculate(orderList, sellerId)
    val lowStock = productList.filter { it.active && (it.lowStockLabel != null || it.stock == 0L) }
    val colors = MaterialTheme.colorScheme
    val dark = isAppDarkTheme()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Revenue hero
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0A8F63), Color(0xFF066B4A))))
        ) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 30.dp)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Column(Modifier.padding(20.dp)) {
                Text(
                    "REVENUE (DELIVERED)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = androidx.compose.ui.unit.TextUnit(0.1f, androidx.compose.ui.unit.TextUnitType.Em)),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    Money.format(stats.revenuePaise),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    "From ${stats.delivered} delivered ${if (stats.delivered == 1) "order" else "orders"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Order counts
        val amber = if (dark) Color(0xFF5B4300) to Color(0xFFFFE9B8) else Color(0xFFFFE9B8) to Color(0xFF3A2A00)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CountTile("New orders", stats.newOrders, colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f))
            CountTile("In progress", stats.inProgress, amber.first, amber.second, Modifier.weight(1f))
            CountTile("Delivered", stats.delivered, colors.primaryContainer, colors.onPrimaryContainer, Modifier.weight(1f))
        }

        if (lowStock.isNotEmpty()) {
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Needs restocking", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            "${lowStock.size} ${if (lowStock.size == 1) "item" else "items"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSecondaryContainer,
                            modifier = Modifier.clip(RoundedCornerShape(11.dp)).background(colors.secondaryContainer).padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                    lowStock.forEach { product ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AsyncImage(
                                model = product.images.firstOrNull(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceContainer)
                            )
                            Text(product.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), maxLines = 1, modifier = Modifier.weight(1f))
                            Text(
                                text = if (product.isAvailable) product.lowStockLabel.orEmpty() else "Out of stock",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (product.isAvailable) colors.secondary else colors.error
                            )
                        }
                    }
                }
            }
        }

        if (orderList.isNotEmpty()) {
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Recent orders", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    orderList.take(RECENT_ORDERS).forEach { order ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(order.id, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                Text(order.date.formatDate(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                            }
                            StatusPill(order.status)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }
}

@Composable
private fun CountTile(label: String, value: Int, container: Color, content: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = content)
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = content)
    }
}

private const val RECENT_ORDERS = 3
