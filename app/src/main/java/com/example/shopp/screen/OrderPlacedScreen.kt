package com.example.shopp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.SecondaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.domain.Money
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.OrderPlacedViewModel

/** Confirmation shown right after an order is placed. */
@Composable
fun OrderPlacedScreen(
    onTrackOrder: (String) -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderPlacedViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsStateWithLifecycle()
    val firstName by viewModel.firstName.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Box(modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        Confetti()

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl).padding(top = 56.dp, bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(132.dp).clip(CircleShape).background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(92.dp)
                        .shadow(12.dp, CircleShape, ambientColor = colors.primary, spotColor = colors.primary)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(52.dp))
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
            Text(
                "Order placed!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = (if (firstName.isBlank()) "Thanks" else "Thanks, $firstName") +
                    ". We have sent your order to the seller and will keep you posted on every step.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow("Order number", viewModel.orderId)
                    order?.takeIf { it.totalPaise > 0 }?.let { DetailRow("Total", Money.format(it.totalPaise)) }
                    order?.let { DetailRow("Payment", paymentName(it.paymentMethod)) }
                    order?.address?.takeIf { it.isNotBlank() }?.let { DetailRow("Delivering to", it.lineSequence().first()) }
                }
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(text = "Track order", onClick = { onTrackOrder(viewModel.orderId) })
            Spacer(Modifier.height(Spacing.md))
            SecondaryButton(text = "Continue shopping", onClick = onContinueShopping)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.size(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

private fun paymentName(method: String): String = when (method) {
    "COD" -> "Cash on delivery"
    "ONLINE" -> "Paid online"
    else -> method
}

/** A few static dots and dashes that give the page a celebratory feel. */
@Composable
private fun Confetti() {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        Dot(colors.secondary, 12.dp, Modifier.offset(40.dp, 60.dp))
        Dot(colors.primary.copy(alpha = 0.7f), 10.dp, Modifier.offset(310.dp, 90.dp))
        Dot(colors.tertiary, 8.dp, Modifier.offset(88.dp, 170.dp))
        Dot(colors.secondaryContainer, 14.dp, Modifier.offset(290.dp, 180.dp))
        Dash(colors.primary, Modifier.offset(60.dp, 120.dp).rotate(35f))
        Dash(colors.secondary, Modifier.offset(330.dp, 150.dp).rotate(-30f))
    }
}

@Composable
private fun Dot(color: Color, size: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    Box(modifier.size(size).clip(CircleShape).background(color))
}

@Composable
private fun Dash(color: Color, modifier: Modifier) {
    Box(modifier.size(width = 16.dp, height = 6.dp).clip(RoundedCornerShape(3.dp)).background(color))
}
