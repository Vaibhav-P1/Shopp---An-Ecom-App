package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.CartItemView
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.SpaceBetweenButton
import com.example.shopp.domain.Money
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.CartLine
import com.example.shopp.viewmodel.CartViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun CartPage(
    modifier: Modifier = Modifier,
    onCheckout: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { context.showToast(it) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        val lines = (uiState as? UiState.Success)?.data.orEmpty()
        val itemCount = lines.sumOf { it.quantity.toInt() }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = Spacing.lg, bottom = Spacing.sm),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Your cart", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            if (itemCount > 0) {
                Text(
                    text = "$itemCount ${if (itemCount == 1) "item" else "items"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when (val state = uiState) {
            UiState.Loading -> ListSkeleton()
            is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry)
            is UiState.Success -> if (state.data.isEmpty()) {
                EmptyView("Your cart is empty", icon = Icons.Outlined.ShoppingCart)
            } else {
                val subtotal = subtotalPaise(state.data)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(state.data, key = { it.productId }) { line ->
                        CartItemView(
                            line = line,
                            onClick = { onProductClick(line.productId) },
                            onIncrement = { viewModel.increment(line.productId) },
                            onDecrement = { viewModel.decrement(line.productId) },
                            onRemove = { viewModel.remove(line.productId) }
                        )
                    }
                    item {
                        ShoppCard(container = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                SummaryLine("Subtotal", Money.format(subtotal))
                                SummaryLine("Delivery", "Free", valueColor = MaterialTheme.colorScheme.primary, bold = true)
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Text(
                                    "Coupons and tax are added at checkout",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
                SpaceBetweenButton(
                    start = "Checkout",
                    end = Money.format(subtotal),
                    onClick = onCheckout,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = Spacing.md)
                )
            }
        }
    }
}

private fun subtotalPaise(lines: List<CartLine>): Long = lines.sumOf { line ->
    (line.product?.let { Money.parsePaise(it.actualPrice) } ?: 0L) * line.quantity
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    bold: Boolean = false
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal),
            color = valueColor
        )
    }
}
