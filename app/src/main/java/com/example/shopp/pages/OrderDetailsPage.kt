package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.shopp.AppUtil
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.statusLabel
import com.example.shopp.domain.Money
import com.example.shopp.model.OrderLineModel
import com.example.shopp.model.OrderModel
import com.example.shopp.viewmodel.OrderDetailsViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun OrderDetailsPage(
    modifier: Modifier = Modifier,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        UiState.Loading -> LoadingView(modifier)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> OrderDetailsContent(state.data, modifier)
    }
}

@Composable
private fun OrderDetailsContent(order: OrderModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Order ${order.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Placed on ${AppUtil.formatDate(order.date)}", fontSize = 14.sp)
            Text(
                text = statusLabel(order.status),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (order.lines.isEmpty()) {
            item {
                Text(
                    "${order.items.values.sum()} item(s). Item details are not available for this order.",
                    fontSize = 14.sp
                )
            }
        } else {
            items(order.lines, key = { it.productId }) { line -> OrderLineRow(line) }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (order.totalPaise > 0) {
                        AmountRow("Subtotal", Money.format(order.subtotalPaise))
                        AmountRow("Discount", "- ${Money.format(order.discountPaise)}")
                        AmountRow("Tax", Money.format(order.taxPaise))
                        HorizontalDivider()
                        AmountRow("Total", Money.format(order.totalPaise), bold = true)
                    }
                    AmountRow("Payment", paymentLabel(order))
                }
            }
        }

        item {
            Text("Delivery address", fontWeight = FontWeight.SemiBold)
            Text(order.address.ifBlank { "Not available" }, fontSize = 14.sp)
        }
    }
}

@Composable
private fun OrderLineRow(line: OrderLineModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(
            model = line.imageUrl,
            contentDescription = line.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(Modifier.weight(1f)) {
            Text(line.title, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text("${Money.format(line.unitPricePaise)} × ${line.quantity}", fontSize = 14.sp)
        }
        Text(Money.format(line.unitPricePaise * line.quantity), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AmountRow(label: String, value: String, bold: Boolean = false) {
    val weight = if (bold) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = weight)
        Text(value, fontWeight = weight)
    }
}

private fun paymentLabel(order: OrderModel): String = when (order.paymentMethod) {
    "COD" -> "Cash on delivery"
    "ONLINE" -> "Paid online"
    else -> order.paymentMethod.ifBlank { "Not available" }
}
