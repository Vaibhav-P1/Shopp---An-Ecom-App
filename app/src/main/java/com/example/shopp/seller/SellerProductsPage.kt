package com.example.shopp.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.SecondaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.ShoppFilterChip
import com.example.shopp.components.ShoppTextField
import com.example.shopp.domain.ProductDraft
import com.example.shopp.domain.ProductDraftValidator
import com.example.shopp.domain.isAvailable
import com.example.shopp.domain.lowStockLabel
import com.example.shopp.model.CategoryModel
import com.example.shopp.model.ProductModel
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.ProductEditorState
import com.example.shopp.viewmodel.UiState

/** Header subtitle such as "3 listed, 2 visible". */
fun productCountLabel(products: List<ProductModel>): String =
    "${products.size} listed · ${products.count { it.active }} visible"

/** The seller's product list with a visibility switch, edit and delete per product. */
@Composable
fun SellerProductsPage(
    state: UiState<List<ProductModel>>,
    onRetry: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (ProductModel) -> Unit,
    onToggleActive: (ProductModel, Boolean) -> Unit,
    onDelete: (ProductModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDelete by remember { mutableStateOf<ProductModel?>(null) }
    var query by remember { mutableStateOf("") }
    pendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete product?") },
            text = { Text("\"${product.title}\" will be removed from the shop. Existing orders keep their own copy.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(product)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }

    Column(modifier.fillMaxSize()) {
        when (state) {
            UiState.Loading -> ListSkeleton(Modifier.weight(1f).padding(Spacing.lg), rows = 4)
            is UiState.Error -> ErrorView(state.message, onRetry = onRetry, modifier = Modifier.weight(1f))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyView(
                        message = "You have not listed any products yet",
                        icon = Icons.Outlined.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    val shown = state.data.filter { it.title.contains(query.trim(), ignoreCase = true) }
                    PillSearchField(
                        value = query,
                        onValueChange = { query = it },
                        hint = "Search your products",
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                    if (shown.isEmpty()) {
                        EmptyView("No products match \"${query.trim()}\"", modifier = Modifier.weight(1f))
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(shown, key = { it.id }) { product ->
                                SellerProductRow(
                                    product = product,
                                    onEdit = { onEdit(product) },
                                    onToggleActive = { onToggleActive(product, it) },
                                    onDelete = { pendingDelete = product }
                                )
                            }
                        }
                    }
                }
            }
        }
        PrimaryButton(
            text = "Add product",
            onClick = onAdd,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )
    }
}

@Composable
private fun PillSearchField(value: String, onValueChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(hint) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun SellerProductRow(
    product: ProductModel,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ShoppCard(modifier = Modifier.fillMaxWidth().alpha(if (product.active) 1f else 0.7f)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceContainer)
                )
                Column(Modifier.weight(1f)) {
                    Text(product.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
                    Text(
                        "₹${product.actualPrice}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Text(
                        text = stockText(product),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (product.isAvailable && product.lowStockLabel == null) FontWeight.Normal else FontWeight.SemiBold
                        ),
                        color = when {
                            !product.isAvailable -> colors.error
                            product.lowStockLabel != null -> colors.secondary
                            else -> colors.onSurfaceVariant
                        }
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(checked = product.active, onCheckedChange = onToggleActive)
                    Text(if (product.active) "Visible" else "Hidden", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
            ) {
                ActionChip(Icons.Default.Edit, "Edit", "Edit ${product.title}", onEdit)
                ActionChip(Icons.Default.Delete, "Delete", "Delete ${product.title}", onDelete)
            }
        }
    }
}

@Composable
private fun ActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, description: String, onClick: () -> Unit) {
    Row(
        Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClickLabel = description, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}

private fun stockText(product: ProductModel): String = when {
    product.stock == null -> "Stock not tracked"
    product.stock <= 0 -> "Out of stock"
    else -> product.lowStockLabel ?: "${product.stock} in stock"
}

/** Full-screen form to create or edit a product; all state lives in the ViewModel. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductEditor(
    state: ProductEditorState,
    categories: List<CategoryModel>,
    canUploadImages: Boolean,
    onDraftChange: ((ProductDraft) -> ProductDraft) -> Unit,
    onPickImages: (List<Uri>) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft = state.draft
    val errors = state.errors
    val colors = MaterialTheme.colorScheme
    var newImageUrl by remember { mutableStateOf("") }
    val imageRoom = ProductDraftValidator.MAX_IMAGES - draft.images.size
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ProductDraftValidator.MAX_IMAGES)
    ) { uris -> if (uris.isNotEmpty()) onPickImages(uris) }

    Column(modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                "Photos (up to ${ProductDraftValidator.MAX_IMAGES})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(top = 6.dp, end = 6.dp)) {
                items(draft.images, key = { it }) { url ->
                    Box {
                        AsyncImage(
                            model = url,
                            contentDescription = "Product image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(colors.surfaceContainer)
                        )
                        if (url == draft.images.first()) {
                            Text(
                                "Cover",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                                    .padding(horizontal = 7.dp, vertical = 1.dp)
                            )
                        }
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceContainerLowest)
                                .clickable(onClickLabel = "Remove image", role = Role.Button) {
                                    onDraftChange { it.copy(images = it.images - url) }
                                },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, contentDescription = "Remove image", modifier = Modifier.size(14.dp)) }
                    }
                }
                items(state.uploadingImages) {
                    Box(
                        Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(colors.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 3.dp) }
                }
                if (canUploadImages && imageRoom > state.uploadingImages) {
                    item {
                        val stroke = colors.outline
                        Column(
                            Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .drawBehind {
                                    drawRoundRect(
                                        color = stroke,
                                        cornerRadius = CornerRadius(18.dp.toPx()),
                                        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                                    )
                                }
                                .clickable(onClickLabel = "Upload from gallery", role = Role.Button) {
                                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary)
                            Text("Upload", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.primary)
                        }
                    }
                }
            }
            errors.images?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodySmall) }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.Top) {
                ShoppTextField(
                    newImageUrl, { newImageUrl = it }, "Or paste an image link",
                    keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done, modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "Add",
                    onClick = {
                        val url = newImageUrl.trim()
                        onDraftChange { it.copy(images = it.images + url) }
                        newImageUrl = ""
                    },
                    enabled = newImageUrl.trim().startsWith("https://") && imageRoom > 0,
                    modifier = Modifier.size(width = 90.dp, height = 56.dp)
                )
            }

            ShoppTextField(draft.title, { v -> onDraftChange { it.copy(title = v) } }, "Title", errorMessage = errors.title)
            ShoppTextField(draft.description, { v -> onDraftChange { it.copy(description = v) } }, "Description")

            Text("Category", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                categories.forEach { category ->
                    ShoppFilterChip(
                        label = category.name,
                        selected = draft.category == category.id,
                        onClick = { onDraftChange { it.copy(category = category.id) } }
                    )
                }
            }
            errors.category?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodySmall) }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                ShoppTextField(
                    draft.price, { v -> onDraftChange { it.copy(price = v) } }, "List price (₹)",
                    keyboardType = KeyboardType.Decimal, errorMessage = errors.price, modifier = Modifier.weight(1f)
                )
                ShoppTextField(
                    draft.actualPrice, { v -> onDraftChange { it.copy(actualPrice = v) } }, "Selling price (₹)",
                    keyboardType = KeyboardType.Decimal, errorMessage = errors.actualPrice, modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.Top) {
                ShoppTextField(
                    draft.stock, { v -> onDraftChange { it.copy(stock = v.filter(Char::isDigit)) } }, "Units in stock",
                    keyboardType = KeyboardType.Number, errorMessage = errors.stock, modifier = Modifier.weight(1f)
                )
                Row(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerLowest)
                        .border(1.5.dp, colors.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Visible", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    Switch(checked = draft.active, onCheckedChange = { v -> onDraftChange { it.copy(active = v) } })
                }
            }
            Spacer(Modifier.height(Spacing.sm))
        }

        Surface(color = colors.background) {
            PrimaryButton(
                text = if (state.productId == null) "Add product" else "Save changes",
                onClick = onSave,
                enabled = state.uploadingImages == 0,
                isLoading = state.isSaving,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
            )
        }
    }
}
