package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.ui.showToast
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.SecondaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.ShoppTextField
import com.example.shopp.data.PaymentMethod
import com.example.shopp.domain.Money
import com.example.shopp.model.AddressModel
import com.example.shopp.payment.PaymentLauncher
import com.example.shopp.payment.findActivity
import com.example.shopp.viewmodel.CheckoutData
import com.example.shopp.viewmodel.CheckoutUiState
import com.example.shopp.viewmodel.CheckoutEvent
import com.example.shopp.viewmodel.CheckoutViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun CheckoutPage(
    modifier: Modifier = Modifier,
    onOrderPlaced: () -> Unit,
    onManageAddresses: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var placedOrderId by remember { mutableStateOf<String?>(null) }

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
                is CheckoutEvent.OrderPlaced -> placedOrderId = event.orderId
                is CheckoutEvent.Message -> context.showToast(event.text)
            }
        }
    }

    placedOrderId?.let { orderId ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Order placed") },
            text = { Text("Thank you! Your order $orderId has been placed.") },
            confirmButton = { TextButton(onClick = onOrderPlaced) { Text("OK") } }
        )
    }

    when (val content = uiState.content) {
        UiState.Loading -> LoadingView(modifier)
        is UiState.Error -> ErrorView(content.message, onRetry = viewModel::retry, modifier = modifier)
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
            onPlaceOrder = viewModel::placeOrder,
            modifier = modifier
        )
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
    val canOrder = state.canOrder
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Checkout",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Deliver to", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onManageAddresses) { Text(if (address == null) "Add address" else "Change") }
            }
            if (address != null) {
                Text(text = address.label, fontWeight = FontWeight.Medium)
                Text(text = address.formatted)
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            if (data.lines.isEmpty()) {
                Text("Your cart is empty")
            }
            data.lines.forEach { line ->
                RowCheckoutItems(
                    title = "${line.title} × ${line.quantity}",
                    value = line.unitPricePaise?.let { Money.format(it * line.quantity) } ?: "—",
                    titleSize = 16,
                    valueSize = 16
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            CouponSection(state = state, actions = coupon)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Billing Rows Breakdown
            RowCheckoutItems(title = "Subtotal", value = Money.format(breakdown.subtotalPaise))
            Spacer(modifier = Modifier.height(8.dp))
            RowCheckoutItems(
                title = state.appliedCoupon?.let { "Discount (${it.coupon.code})" } ?: "Discount",
                value = "-" + Money.format(breakdown.discountPaise)
            )
            Spacer(modifier = Modifier.height(8.dp))
            RowCheckoutItems(title = "Tax", value = "+" + Money.format(breakdown.taxPaise))

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Final Aggregate Row Outputs
            Text(
                text = "Total To Pay",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Text(
                text = Money.format(breakdown.totalPaise),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Payment method", fontWeight = FontWeight.SemiBold)
            Column(modifier = Modifier.selectableGroup()) {
                PaymentOption(
                    label = "Cash on delivery",
                    selected = paymentMethod == PaymentMethod.COD,
                    enabled = !isPlacing,
                    onClick = { onPaymentMethodSelected(PaymentMethod.COD) }
                )
                PaymentOption(
                    label = "Pay online (Razorpay test mode)",
                    selected = paymentMethod == PaymentMethod.ONLINE,
                    enabled = !isPlacing,
                    onClick = { onPaymentMethodSelected(PaymentMethod.ONLINE) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val problem = when {
            data.lines.isEmpty() -> null
            address == null -> "Add a delivery address to place an order"
            data.hasUnavailableItems -> "Remove unavailable items from your cart to continue"
            else -> null
        }
        if (problem != null) {
            Text(
                text = problem,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        PrimaryButton(
            text = if (paymentMethod == PaymentMethod.COD) "Place Order" else "Pay Now",
            onClick = onPlaceOrder,
            enabled = canOrder,
            isLoading = isPlacing
        )
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
    Text(text = "Coupon", fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    if (applied != null) {
        ShoppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = applied.coupon.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "You save ${Money.format(applied.discountPaise)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = actions.onRemove, enabled = !state.isPlacing) { Text("Remove") }
            }
        }
    } else {
        Row(verticalAlignment = Alignment.Top) {
            ShoppTextField(
                value = state.couponInput,
                onValueChange = actions.onInputChange,
                label = "Coupon code",
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
                modifier = Modifier.width(100.dp).padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PaymentOption(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Text(text = label, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
fun RowCheckoutItems(title: String, value: String, titleSize: Int = 20, valueSize: Int = 18) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = titleSize.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, fontSize = valueSize.sp)
    }
}
