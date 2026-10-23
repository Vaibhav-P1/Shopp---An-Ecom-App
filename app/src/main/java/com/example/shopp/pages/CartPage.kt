package com.example.shopp.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.ui.showToast
import com.example.shopp.components.CartItemView
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Cart",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        when (val state = uiState) {
            UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry)
            is UiState.Success -> if (state.data.isEmpty()) {
                EmptyView("No items in cart")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.data, key = { it.productId }) { line ->
                        CartItemView(
                            line = line,
                            onClick = { onProductClick(line.productId) },
                            onIncrement = { viewModel.increment(line.productId) },
                            onDecrement = { viewModel.decrement(line.productId) },
                            onRemove = { viewModel.remove(line.productId) }
                        )
                    }
                }
                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Checkout", fontSize = 16.sp)
                }
            }
        }
    }
}
