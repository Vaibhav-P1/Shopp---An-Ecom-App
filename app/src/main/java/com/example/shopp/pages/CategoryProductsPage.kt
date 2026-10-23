package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.ProductItemView
import com.example.shopp.components.rememberAddToCart
import com.example.shopp.viewmodel.CategoryProductsViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun CategoryProductsPage(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit,
    viewModel: CategoryProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val addToCart = rememberAddToCart()

    when (val state = uiState) {
        UiState.Loading -> LoadingView(modifier)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyView("No products in this category yet", modifier)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.data, key = { it.id }) { product ->
                    ProductItemView(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { addToCart(product.id) }
                    )
                }
            }
        }
    }
}
