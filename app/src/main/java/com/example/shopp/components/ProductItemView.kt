package com.example.shopp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.domain.discountPercent
import com.example.shopp.domain.isAvailable
import com.example.shopp.domain.lowStockLabel
import com.example.shopp.domain.unavailableLabel
import com.example.shopp.model.ProductModel

/**
 * Product card for grids and rows. The heart is shown when [onToggleFavorite] is given.
 * Unavailable products are dimmed and carry a dark status badge instead of the discount badge.
 */
@Composable
fun ProductItemView(
    product: ProductModel,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .padding(4.dp)
            .clickable(onClickLabel = "View ${product.title}", role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, colors.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(Modifier.padding(10.dp)) {
            Box {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerLowest)
                        .alpha(if (product.isAvailable) 1f else 0.5f)
                )
                val badge = if (product.isAvailable) product.discountPercent?.let { "$it% OFF" } else product.unavailableLabel
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (product.isAvailable) colors.onSecondary else colors.surface,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (product.isAvailable) colors.secondary else colors.onSurface)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (onToggleFavorite != null) {
                    Box(
                        Modifier
                            .padding(6.dp)
                            .align(Alignment.TopEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerLowest)
                            .clickable(
                                onClickLabel = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                role = Role.Button,
                                onClick = onToggleFavorite
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) colors.secondary else colors.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(if (product.isAvailable) 1f else 0.6f)
            )

            product.lowStockLabel?.takeIf { product.isAvailable }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.weight(1f).alpha(if (product.isAvailable) 1f else 0.6f),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "₹${product.actualPrice}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (product.price != product.actualPrice) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "₹${product.price}",
                            style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = colors.outline
                        )
                    }
                }
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (product.isAvailable) colors.primary else colors.surfaceContainerHighest)
                        .clickable(
                            enabled = product.isAvailable,
                            onClickLabel = "Add to cart",
                            role = Role.Button,
                            onClick = onAddToCart
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to cart",
                        tint = if (product.isAvailable) colors.onPrimary else colors.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
