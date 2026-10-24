package com.example.shopp.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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

    Column(modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
        when (state) {
            UiState.Loading -> ListSkeleton(Modifier.weight(1f).padding(top = Spacing.lg), rows = 4)
            is UiState.Error -> ErrorView(state.message, onRetry = onRetry, modifier = Modifier.weight(1f))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyView(
                        message = "You have not listed any products yet",
                        icon = Icons.Outlined.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(state.data, key = { it.id }) { product ->
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
        PrimaryButton(text = "Add product", onClick = onAdd, modifier = Modifier.padding(vertical = Spacing.md))
    }
}

@Composable
private fun SellerProductRow(
    product: ProductModel,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    ShoppCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                Text(product.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text("₹${product.actualPrice}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stockText(product),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (product.isAvailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(checked = product.active, onCheckedChange = onToggleActive)
                Text(if (product.active) "Visible" else "Hidden", style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit ${product.title}") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete ${product.title}") }
        }
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
    var newImageUrl by remember { mutableStateOf("") }
    val imageRoom = ProductDraftValidator.MAX_IMAGES - draft.images.size
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ProductDraftValidator.MAX_IMAGES)
    ) { uris -> if (uris.isNotEmpty()) onPickImages(uris) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        ShoppTextField(draft.title, { v -> onDraftChange { it.copy(title = v) } }, "Title", errorMessage = errors.title)
        ShoppTextField(draft.description, { v -> onDraftChange { it.copy(description = v) } }, "Description")

        Text("Category", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            categories.forEach { category ->
                FilterChip(
                    selected = draft.category == category.id,
                    onClick = { onDraftChange { it.copy(category = category.id) } },
                    label = { Text(category.name) }
                )
            }
        }
        errors.category?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

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
        ShoppTextField(
            draft.stock, { v -> onDraftChange { it.copy(stock = v.filter(Char::isDigit)) } }, "Units in stock",
            keyboardType = KeyboardType.Number, errorMessage = errors.stock
        )

        Text("Images", style = MaterialTheme.typography.titleSmall)
        if (draft.images.isNotEmpty() || state.uploadingImages > 0) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(draft.images, key = { it }) { url ->
                    Box {
                        AsyncImage(
                            model = url,
                            contentDescription = "Product image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { onDraftChange { it.copy(images = it.images - url) } },
                            modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                items(state.uploadingImages) {
                    Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    }
                }
            }
        }
        errors.images?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        if (canUploadImages) {
            SecondaryButton(
                text = "Upload from gallery",
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = imageRoom > state.uploadingImages
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.Top) {
            ShoppTextField(
                newImageUrl, { newImageUrl = it }, "Or paste an image URL",
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Visible to customers", modifier = Modifier.weight(1f))
            Switch(checked = draft.active, onCheckedChange = { v -> onDraftChange { it.copy(active = v) } })
        }

        Spacer(Modifier.height(Spacing.sm))
        PrimaryButton(
            text = if (state.productId == null) "Add product" else "Save changes",
            onClick = onSave,
            enabled = state.uploadingImages == 0,
            isLoading = state.isSaving
        )
        Spacer(Modifier.height(Spacing.lg))
    }
}
