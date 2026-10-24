package com.example.shopp.seller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = Spacing.lg),
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
                Switch(
                    checked = product.active,
                    onCheckedChange = onToggleActive
                )
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

/** Full-screen form to create ([product] == null) or edit a product. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductEditor(
    product: ProductModel?,
    categories: List<CategoryModel>,
    state: ProductEditorState,
    onSave: (ProductDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember(product?.id) {
        mutableStateOf(
            product?.let {
                ProductDraft(
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    price = it.price,
                    actualPrice = it.actualPrice,
                    stock = it.stock?.toString().orEmpty(),
                    images = it.images,
                    active = it.active
                )
            } ?: ProductDraft()
        )
    }
    var newImageUrl by remember { mutableStateOf("") }
    val errors = state.errors

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        ShoppTextField(draft.title, { draft = draft.copy(title = it) }, "Title", errorMessage = errors.title)
        ShoppTextField(draft.description, { draft = draft.copy(description = it) }, "Description")

        Text("Category", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            categories.forEach { category ->
                FilterChip(
                    selected = draft.category == category.id,
                    onClick = { draft = draft.copy(category = category.id) },
                    label = { Text(category.name) }
                )
            }
        }
        errors.category?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ShoppTextField(
                draft.price, { draft = draft.copy(price = it) }, "List price (₹)",
                keyboardType = KeyboardType.Decimal, errorMessage = errors.price, modifier = Modifier.weight(1f)
            )
            ShoppTextField(
                draft.actualPrice, { draft = draft.copy(actualPrice = it) }, "Selling price (₹)",
                keyboardType = KeyboardType.Decimal, errorMessage = errors.actualPrice, modifier = Modifier.weight(1f)
            )
        }
        ShoppTextField(
            draft.stock, { draft = draft.copy(stock = it.filter(Char::isDigit)) }, "Units in stock",
            keyboardType = KeyboardType.Number, errorMessage = errors.stock
        )

        Text("Images", style = MaterialTheme.typography.titleSmall)
        if (draft.images.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(draft.images, key = { it }) { url ->
                    androidx.compose.foundation.layout.Box {
                        AsyncImage(
                            model = url,
                            contentDescription = "Product image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { draft = draft.copy(images = draft.images - url) },
                            modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.Top) {
            ShoppTextField(
                newImageUrl, { newImageUrl = it }, "Image URL",
                keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done,
                errorMessage = errors.images, modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                text = "Add",
                onClick = {
                    val url = newImageUrl.trim()
                    if (url.startsWith("https://") && draft.images.size < ProductDraftValidator.MAX_IMAGES) {
                        draft = draft.copy(images = draft.images + url)
                        newImageUrl = ""
                    }
                },
                enabled = newImageUrl.trim().startsWith("https://"),
                modifier = Modifier.size(width = 90.dp, height = 56.dp).padding(top = 0.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Visible to customers", modifier = Modifier.weight(1f))
            Switch(checked = draft.active, onCheckedChange = { draft = draft.copy(active = it) })
        }

        Spacer(Modifier.height(Spacing.sm))
        PrimaryButton(
            text = if (product == null) "Add product" else "Save changes",
            onClick = { onSave(draft) },
            isLoading = state.isSaving
        )
        Spacer(Modifier.height(Spacing.lg))
    }
}
