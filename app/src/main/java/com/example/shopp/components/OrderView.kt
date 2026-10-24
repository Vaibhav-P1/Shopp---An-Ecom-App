package com.example.shopp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.ui.formatDate
import com.example.shopp.domain.Money
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus

@Composable
fun OrderView(orderItem: OrderModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ShoppCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID: ${orderItem.id}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${orderItem.date.formatDate()}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = statusLabel(orderItem.status),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (orderItem.status == OrderStatus.CANCELLED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Items: ${orderItem.items.values.sum()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (orderItem.totalPaise > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: ${Money.format(orderItem.totalPaise)} · ${orderItem.paymentMethod}",
                    fontSize = 14.sp
                )
            }

        }
    }
}

/** User-facing name for a stored order status. */
fun statusLabel(status: String): String = OrderStatus.label(status)
