package com.example.shopp.pages

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.ProductItemView
import com.example.shopp.components.RatingSummaryLine
import com.example.shopp.components.ReviewsSection
import com.example.shopp.components.rememberAddToCart
import com.example.shopp.domain.discountPercent
import com.example.shopp.domain.isAvailable
import com.example.shopp.domain.lowStockLabel
import com.example.shopp.domain.unavailableLabel
import com.example.shopp.model.ProductModel
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.ProductDetailsViewModel
import com.example.shopp.viewmodel.ProductReviewsViewModel
import com.example.shopp.viewmodel.ReviewsUiState
import com.example.shopp.viewmodel.UiState

@Composable
fun ProductDetailsPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    reviewsViewModel: ProductReviewsViewModel = hiltViewModel(),
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val productId = viewModel.productId
    val addToCart = rememberAddToCart()

    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val similar by viewModel.similar.collectAsStateWithLifecycle()

    val reviews by reviewsViewModel.uiState.collectAsStateWithLifecycle()
    val isSavingReview by reviewsViewModel.isSaving.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { context.showToast(it) }
    }
    LaunchedEffect(reviewsViewModel) {
        reviewsViewModel.messages.collect { context.showToast(it) }
    }

    when (val state = uiState) {
        UiState.Loading -> LoadingView(modifier.statusBarsPadding())
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier.statusBarsPadding())
        is UiState.Success -> ProductDetailsContent(
            product = state.data,
            isFavorite = isFavorite,
            onBack = onBack,
            onToggleFavorite = viewModel::toggleFavorite,
            onAddToCart = { addToCart(productId) },
            similar = similar,
            onProductClick = onProductClick,
            onAddSimilarToCart = addToCart,
            reviews = reviews,
            isSavingReview = isSavingReview,
            onSubmitReview = reviewsViewModel::submit,
            onDeleteReview = reviewsViewModel::deleteMine,
            modifier = modifier
        )
    }
}

private fun shareProduct(context: Context, product: ProductModel) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${product.title} for ₹${product.actualPrice} on Shopp")
    }
    context.startActivity(Intent.createChooser(intent, "Share product"))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailsContent(
    product: ProductModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCart: () -> Unit,
    similar: List<ProductModel>,
    onProductClick: (String) -> Unit,
    onAddSimilarToCart: (String) -> Unit,
    reviews: ReviewsUiState,
    isSavingReview: Boolean,
    onSubmitReview: (Int, String) -> Unit,
    onDeleteReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Column(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Image gallery under the status bar, with round buttons floating on top
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(colors.surfaceContainerLowest)
            ) {
                val pagerState = rememberPagerState(0) { product.images.size }
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    AsyncImage(
                        model = product.images[page],
                        contentDescription = "${product.title}, image ${page + 1} of ${product.images.size}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FloatingRoundButton(onClick = onBack, label = "Back") {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FloatingRoundButton(onClick = { shareProduct(context, product) }, label = "Share ${product.title}") {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                        FloatingRoundButton(
                            onClick = onToggleFavorite,
                            label = if (isFavorite) "Remove from favorites" else "Add to favorites"
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                tint = if (isFavorite) colors.secondary else colors.onSurface,
                                contentDescription = null
                            )
                        }
                    }
                }
                if (product.images.size > 1) {
                    Row(
                        Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(product.images.size) { index ->
                            val selected = index == pagerState.currentPage
                            Box(
                                Modifier
                                    .height(6.dp)
                                    .width(if (selected) 20.dp else 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (selected) colors.primary else colors.primary.copy(alpha = 0.35f))
                            )
                        }
                    }
                }
            }

            Column(
                Modifier.padding(horizontal = 20.dp).padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "₹${product.actualPrice}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    if (product.price != product.actualPrice) {
                        Text(
                            text = "₹${product.price}",
                            style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                            color = colors.outline
                        )
                    }
                    product.discountPercent?.let { percent ->
                        Surface(shape = RoundedCornerShape(13.dp), color = colors.secondaryContainer) {
                            Text(
                                text = "$percent% OFF",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingSummaryLine(reviews.summary, modifier = Modifier.weight(1f))
                    product.lowStockLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.secondary
                        )
                    }
                }

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6f),
                    color = colors.onSurfaceVariant
                )

                if (product.otherDetails.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        maxItemsInEachRow = 3
                    ) {
                        product.otherDetails.forEach { (key, value) ->
                            Column(
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(key, style = MaterialTheme.typography.labelSmall, color = colors.outline)
                                Text(value, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }

                if (similar.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text("You may also like", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(similar, key = { it.id }) { other ->
                            ProductItemView(
                                product = other,
                                onClick = { onProductClick(other.id) },
                                onAddToCart = { onAddSimilarToCart(other.id) },
                                modifier = Modifier.width(190.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xs))
                ReviewsSection(
                    state = reviews,
                    isSaving = isSavingReview,
                    onSubmit = onSubmitReview,
                    onDeleteMine = onDeleteReview
                )
                Spacer(Modifier.height(Spacing.lg))
            }
        }

        // Sticky purchase bar
        Surface(color = colors.surfaceContainerLowest, shadowElevation = 12.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Price", style = MaterialTheme.typography.labelSmall, color = colors.outline)
                    Text(
                        "₹${product.actualPrice}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Button(
                    onClick = onAddToCart,
                    enabled = product.isAvailable,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    if (product.isAvailable) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = if (product.isAvailable) "Add to cart" else product.unavailableLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingRoundButton(onClick: () -> Unit, label: String, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Box(Modifier.size(24.dp).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
