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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.OrderView
import com.example.shopp.components.ShoppFilterChip
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.OrdersViewModel
import com.example.shopp.viewmodel.UiState

private enum class OrderFilter(val label: String) {
    All("All"),
    Active("Active"),
    Delivered("Delivered"),
    Cancelled("Cancelled");

    fun matches(order: OrderModel): Boolean = when (this) {
        All -> true
        Active -> order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED
        Delivered -> order.status == OrderStatus.DELIVERED
        Cancelled -> order.status == OrderStatus.CANCELLED
    }
}

@Composable
fun OrdersPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(OrderFilter.All) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("My orders", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }

        when (val state = uiState) {
            UiState.Loading -> ListSkeleton()
            is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry)
            is UiState.Success -> if (state.data.isEmpty()) {
                EmptyView("You haven't placed any orders yet", icon = Icons.Outlined.Receipt)
            } else {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OrderFilter.entries.forEach { option ->
                        ShoppFilterChip(label = option.label, selected = filter == option, onClick = { filter = option })
                    }
                }
                val shown = state.data.filter(filter::matches)
                if (shown.isEmpty()) {
                    EmptyView("No ${filter.label.lowercase()} orders", icon = Icons.Outlined.Receipt)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(shown, key = { it.id }) { order ->
                            OrderView(orderItem = order, onClick = { onOrderClick(order.id) })
                        }
                    }
                }
            }
        }
    }
}
