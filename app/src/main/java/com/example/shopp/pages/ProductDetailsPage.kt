package com.example.shopp.pages

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.shopp.ui.showToast
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.ProductItemView
import com.example.shopp.components.RatingSummaryLine
import com.example.shopp.domain.discountPercent
import com.example.shopp.components.ReviewsSection
import com.example.shopp.components.rememberAddToCart
import com.example.shopp.model.ProductModel
import com.example.shopp.viewmodel.ProductDetailsViewModel
import com.example.shopp.viewmodel.ProductReviewsViewModel
import com.example.shopp.viewmodel.ReviewsUiState
import com.example.shopp.viewmodel.UiState
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType

@Composable
fun ProductDetailsPage(
    modifier: Modifier = Modifier,
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
        UiState.Loading -> LoadingView(modifier)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> ProductDetailsContent(
            product = state.data,
            isFavorite = isFavorite,
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

@Composable
private fun ProductDetailsContent(
    product: ProductModel,
    isFavorite: Boolean,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Product Title
        Text(
            text = product.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column {
            val pagerState = rememberPagerState(0) { product.images.size }
            HorizontalPager(
                state = pagerState,
                pageSpacing = 24.dp,
            ) {
                AsyncImage(
                    model = product.images[it],
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            DotsIndicator(
                dotCount = product.images.size,
                type = ShiftIndicatorType(
                    DotGraphic(
                        color = MaterialTheme.colorScheme.primary,
                        size = 6.dp
                    )
                ),
                pagerState = pagerState
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Original slashed price
            Text(
                text = "₹${product.price}",
                fontSize = 16.sp,
                style = TextStyle(textDecoration = TextDecoration.LineThrough)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Actual discounted price
            Text(
                text = "₹${product.actualPrice}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            product.discountPercent?.let { percent ->
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        text = "$percent% OFF",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val context = LocalContext.current
            IconButton(onClick = { shareProduct(context, product) }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share ${product.title}")
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
                )
            }
        }
        RatingSummaryLine(reviews.summary)
        Spacer(modifier = Modifier.height(8.dp))

        // Add to Cart Primary Button
        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Add to Cart", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Description
        Text(
            text = "Product Description",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.description,
            fontSize = 16.sp
        )

        // Dynamic Product Metadata (Map items loop)
        if (product.otherDetails.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Other Product Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            product.otherDetails.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$key: ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = value,
                        fontSize = 16.sp
                    )
                }
            }
        }

        if (similar.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "You may also like", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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

        Spacer(modifier = Modifier.height(24.dp))
        ReviewsSection(
            state = reviews,
            isSaving = isSavingReview,
            onSubmit = onSubmitReview,
            onDeleteMine = onDeleteReview
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
