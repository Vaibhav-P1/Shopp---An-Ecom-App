package com.example.shopp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.SecondaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.ShoppTextField
import com.example.shopp.components.SpaceBetweenButton
import com.example.shopp.data.PaymentMethod
import com.example.shopp.domain.Money
import com.example.shopp.payment.PaymentLauncher
import com.example.shopp.payment.findActivity
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.CheckoutData
import com.example.shopp.viewmodel.CheckoutEvent
import com.example.shopp.viewmodel.CheckoutUiState
import com.example.shopp.viewmodel.CheckoutViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun CheckoutPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onManageAddresses: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.StartPayment -> {
                    val activity = context.findActivity()
                    if (activity == null) {
                        context.showToast("Could not open the payment screen")
                    } else {
                        PaymentLauncher.start(activity, event.amountPaise, event.orderId)
                    }
                }
                is CheckoutEvent.OrderPlaced -> onOrderPlaced(event.orderId)
                is CheckoutEvent.Message -> context.showToast(event.text)
            }
        }
    }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Checkout", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }

        when (val content = uiState.content) {
            UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(content.message, onRetry = viewModel::retry)
            is UiState.Success -> CheckoutContent(
                data = content.data,
                state = uiState,
                coupon = CouponActions(
                    onInputChange = viewModel::onCouponInputChange,
                    onApply = viewModel::applyCoupon,
                    onRemove = viewModel::removeCoupon
                ),
                onManageAddresses = onManageAddresses,
                paymentMethod = uiState.paymentMethod,
                isPlacing = uiState.isPlacing,
                onPaymentMethodSelected = viewModel::selectPaymentMethod,
                onPlaceOrder = viewModel::placeOrder
            )
        }
    }
}

@Composable
private fun CheckoutContent(
    data: CheckoutData,
    state: CheckoutUiState,
    coupon: CouponActions,
    onManageAddresses: () -> Unit,
    paymentMethod: PaymentMethod,
    isPlacing: Boolean,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val address = state.address
    val breakdown = state.breakdown
    val colors = MaterialTheme.colorScheme

    Column(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Delivery address
            ShoppCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile(Icons.Default.LocationOn)
                    Column(Modifier.weight(1f)) {
                        if (address != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(address.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text(
                                    "DEFAULT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                address.formatted,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5f),
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            Text("No delivery address", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Add one to place your order", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = if (address == null) "Add" else "Change",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primary,
                        modifier = Modifier.clickable(role = Role.Button, onClick = onManageAddresses).padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }

            // Items
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (data.lines.isEmpty()) Text("Your cart is empty", style = MaterialTheme.typography.bodyMedium)
                    data.lines.forEach { line ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AsyncImage(
                                model = line.imageUrl.ifBlank { null },
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceContainer)
                            )
                            Text(
                                text = buildItemLabel(line.title, line.quantity),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = line.unitPricePaise?.let { Money.format(it * line.quantity) } ?: "—",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            CouponSection(state = state, actions = coupon)

            // Payment
            ShoppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Payment", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PaymentOption(
                            label = "Cash on delivery",
                            hint = null,
                            selected = paymentMethod == PaymentMethod.COD,
                            enabled = !isPlacing,
                            onClick = { onPaymentMethodSelected(PaymentMethod.COD) }
                        )
                        PaymentOption(
                            label = "Pay online",
                            hint = "(UPI, cards, wallets)",
                            selected = paymentMethod == PaymentMethod.ONLINE,
                            enabled = !isPlacing,
                            onClick = { onPaymentMethodSelected(PaymentMethod.ONLINE) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        val problem = when {
            data.lines.isEmpty() -> null
            address == null -> "Add a delivery address to place an order"
            data.hasUnavailableItems -> "Remove unavailable items from your cart to continue"
            else -> null
        }

        // Sticky totals and the place-order button
        Surface(color = colors.surfaceContainerLowest, shadowElevation = 12.dp) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TotalsRow("Subtotal", Money.format(breakdown.subtotalPaise), colors.onSurfaceVariant)
                if (breakdown.discountPaise > 0) {
                    TotalsRow(
                        label = state.appliedCoupon?.let { "Discount (${it.coupon.code})" } ?: "Discount",
                        value = "-" + Money.format(breakdown.discountPaise),
                        color = colors.primary,
                        bold = true
                    )
                }
                TotalsRow("Tax", Money.format(breakdown.taxPaise), colors.onSurfaceVariant)
                if (problem != null) {
                    Text(
                        text = problem,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                SpaceBetweenButton(
                    start = if (paymentMethod == PaymentMethod.COD) "Place order" else "Pay now",
                    end = Money.format(breakdown.totalPaise),
                    onClick = onPlaceOrder,
                    enabled = state.canOrder,
                    isLoading = isPlacing,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun buildItemLabel(title: String, quantity: Long) = "$title × $quantity"

@Composable
private fun IconTile(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun TotalsRow(label: String, value: String, color: androidx.compose.ui.graphics.Color, bold: Boolean = false) {
    val style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style, color = color)
        Text(value, style = style, color = color)
    }
}

private class CouponActions(
    val onInputChange: (String) -> Unit,
    val onApply: () -> Unit,
    val onRemove: () -> Unit
)

@Composable
private fun CouponSection(state: CheckoutUiState, actions: CouponActions) {
    val applied = state.appliedCoupon
    val colors = MaterialTheme.colorScheme
    if (applied != null) {
        val stroke = colors.primary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.primaryContainer.copy(alpha = 0.45f))
                .drawBehind {
                    drawRoundRect(
                        color = stroke,
                        cornerRadius = CornerRadius(22.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                    )
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "${applied.coupon.code} applied",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
                Text(
                    "You save ${Money.format(applied.discountPaise)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant
                )
            }
            TextButton(onClick = actions.onRemove, enabled = !state.isPlacing) {
                Text("Remove", color = colors.onSurfaceVariant)
            }
        }
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            ShoppTextField(
                value = state.couponInput,
                onValueChange = actions.onInputChange,
                label = "Coupon code",
                leadingIcon = Icons.Default.LocalOffer,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { actions.onApply() }),
                errorMessage = state.couponError,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            SecondaryButton(
                text = if (state.isApplyingCoupon) "..." else "Apply",
                onClick = actions.onApply,
                enabled = state.couponInput.isNotBlank() && !state.isApplyingCoupon && !state.isPlacing,
                modifier = Modifier.width(100.dp).padding(top = 0.dp)
            )
        }
    }
}

@Composable
private fun PaymentOption(label: String, hint: String?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primaryContainer.copy(alpha = 0.45f) else androidx.compose.ui.graphics.Color.Transparent)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(20.dp)
                .border(if (selected) 6.dp else 2.dp, if (selected) colors.primary else colors.outline, CircleShape)
        )
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal))
        if (hint != null) Text(hint, style = MaterialTheme.typography.labelMedium, color = colors.outline)
    }
}
