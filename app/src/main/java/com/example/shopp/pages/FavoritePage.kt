package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.ProductItemView
import com.example.shopp.components.rememberAddToCart
import com.example.shopp.viewmodel.FavoritesViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun FavoritePage(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val addToCart = rememberAddToCart()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Favorites",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry)
            is UiState.Success -> if (state.data.isEmpty()) {
                EmptyView("No items in favorites")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.data.chunked(2), key = { row -> row.first().id }) { rowItems ->
                        Row {
                            rowItems.forEach {
                                ProductItemView(
                                    product = it,
                                    onClick = { onProductClick(it.id) },
                                    onAddToCart = { addToCart(it.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // A row with a single product gets an empty spacer to keep the column width
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
