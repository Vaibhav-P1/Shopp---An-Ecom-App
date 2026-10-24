package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ProductGridSkeleton
import com.example.shopp.components.ProductItemView
import com.example.shopp.components.rememberAddToCart
import com.example.shopp.viewmodel.CategoryProductsContent
import com.example.shopp.viewmodel.CategoryProductsViewModel
import com.example.shopp.viewmodel.UiState

/** How many rows from the end the next page is requested, so scrolling rarely hits a spinner. */
private const val PREFETCH_DISTANCE = 4

@Composable
fun CategoryProductsPage(
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit,
    viewModel: CategoryProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        UiState.Loading -> ProductGridSkeleton(modifier)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> if (state.data.products.isEmpty()) {
            EmptyView("No products in this category yet", modifier)
        } else {
            ProductGrid(
                content = state.data,
                onProductClick = onProductClick,
                onLoadMore = viewModel::loadMore,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProductGrid(
    content: CategoryProductsContent,
    onProductClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addToCart = rememberAddToCart()
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, content.products.size, content.endReached, content.loadMoreFailed) {
        if (content.endReached || content.loadMoreFailed) return@LaunchedEffect
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= content.products.size - PREFETCH_DISTANCE
        }.collect { nearEnd -> if (nearEnd) onLoadMore() }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(content.products, key = { it.id }) { product ->
            ProductItemView(
                product = product,
                onClick = { onProductClick(product.id) },
                onAddToCart = { addToCart(product.id) }
            )
        }
        if (content.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (content.loadMoreFailed) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onLoadMore) { Text("Couldn't load more. Tap to retry") }
                }
            }
        }
    }
}
