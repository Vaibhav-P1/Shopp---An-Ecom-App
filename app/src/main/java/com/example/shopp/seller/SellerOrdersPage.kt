package com.example.shopp.seller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ShoppCard
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

/** Orders that contain the seller's products, each with a button for the next fulfilment step. */
@Composable
fun SellerOrdersPage(
    state: UiState<List<OrderModel>>,
    sellerId: String,
    updating: Set<String>,
    onRetry: () -> Unit,
    onAdvance: (OrderModel) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        UiState.Loading -> ListSkeleton(modifier.padding(Spacing.lg), rows = 4)
        is UiState.Error -> ErrorView(state.message, onRetry = onRetry, modifier = modifier)
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyView("No orders for your products yet", icon = Icons.Outlined.Receipt, modifier = modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(state.data, key = { it.id }) { order ->
                    SellerOrderCard(order, sellerId, order.id in updating, onAdvance = { onAdvance(order) })
                }
            }
        }
    }
}

@Composable
private fun SellerOrderCard(order: OrderModel, sellerId: String, isUpdating: Boolean, onAdvance: () -> Unit) {
    ShoppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row {
                Text(order.id, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    text = OrderStatus.label(order.status),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (order.status == OrderStatus.CANCELLED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Text(order.date.formatDate(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = Spacing.xs))

            order.linesOf(sellerId).forEach { line ->
                Row {
                    Text("${line.title} × ${line.quantity}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(Money.format(line.unitPricePaise * line.quantity), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(Modifier.padding(top = Spacing.xs)) {
                Text("Your items", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(Money.format(order.revenueOf(sellerId)), fontWeight = FontWeight.SemiBold)
            }

            Text("Ship to", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = Spacing.sm))
            Text(order.address, style = MaterialTheme.typography.bodySmall)

            OrderStatus.actionLabel(order.status)?.let { label ->
                Spacer(Modifier.height(Spacing.sm))
                PrimaryButton(text = label, onClick = onAdvance, isLoading = isUpdating)
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

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text("Hello, $shopName", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard("New orders", stats.newOrders.toString(), Modifier.weight(1f))
            StatCard("In progress", stats.inProgress.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard("Delivered", stats.delivered.toString(), Modifier.weight(1f))
            StatCard("Revenue", Money.format(stats.revenuePaise), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard("Products", productList.size.toString(), Modifier.weight(1f))
            StatCard("Low or out of stock", lowStock.size.toString(), Modifier.weight(1f))
        }

        if (lowStock.isNotEmpty()) {
            Text("Needs restocking", style = MaterialTheme.typography.titleMedium)
            lowStock.forEach { product ->
                Row {
                    Text(product.title, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(
                        text = if (product.isAvailable) product.lowStockLabel.orEmpty() else "Out of stock",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (orderList.isNotEmpty()) {
            Text("Recent orders", style = MaterialTheme.typography.titleMedium)
            orderList.take(RECENT_ORDERS).forEach { order ->
                Row {
                    Column(Modifier.weight(1f)) {
                        Text(order.id, style = MaterialTheme.typography.bodyMedium)
                        Text(order.date.formatDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(OrderStatus.label(order.status), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ShoppCard(modifier = modifier) {
        Column(Modifier.padding(Spacing.lg)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private const val RECENT_ORDERS = 3
