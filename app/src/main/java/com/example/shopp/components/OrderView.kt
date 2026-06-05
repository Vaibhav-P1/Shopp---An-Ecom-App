package com.example.shopp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.AppUtil
import com.example.shopp.model.OrderModel

@Composable
fun OrderView(orderItem: OrderModel, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()

            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                text = "Date: ${AppUtil.formatDate(orderItem.date)}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Displays real-time order progression status tags with an orange hex tint highlight
            Text(
                text = "Status: ${orderItem.status}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100) // Hex equivalent representation for dark orange
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Items: ${orderItem.items.size} Product(s)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

        }
    }
}