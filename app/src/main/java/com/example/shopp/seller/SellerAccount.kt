package com.example.shopp.seller

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SellerAccount(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🚀",
                fontSize = 70.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Seller Account",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}