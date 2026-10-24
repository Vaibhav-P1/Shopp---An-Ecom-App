package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import com.example.shopp.components.ActiveFilterChip
import com.example.shopp.components.FavoriteToggle
import com.example.shopp.components.ShoppFilterChip
import com.example.shopp.components.rememberFavoriteToggle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ProductGridSkeleton
import com.example.shopp.components.ProductItemView
import com.example.shopp.components.SecondaryButton
import com.example.shopp.components.rememberAddToCart
import com.example.shopp.domain.Money
import com.example.shopp.domain.SearchFilters
import com.example.shopp.domain.SortOrder
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.SearchContent
import com.example.shopp.viewmodel.SearchViewModel
import com.example.shopp.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val addToCart = rememberAddToCart()
    val favorites = rememberFavoriteToggle()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier.fillMaxSize()) {
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = {
                viewModel.commitQuery()
                focusManager.clearFocus()
            },
            onBack = onBack,
            activeFilterCount = filters.activeCount,
            onFilterClick = { showFilters = true },
            focusRequester = focusRequester
        )

        ActiveFilters(
            filters = filters,
            categories = (uiState as? UiState.Success)?.data?.categories.orEmpty(),
            onChange = viewModel::applyFilters
        )

        when (val state = uiState) {
            UiState.Loading -> ProductGridSkeleton()
            is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry)
            is UiState.Success -> SearchResults(
                content = state.data,
                query = query,
                recent = recent,
                onRecentClick = {
                    viewModel.useRecent(it)
                    focusManager.clearFocus()
                },
                onRecentRemove = viewModel::removeRecent,
                onRecentClear = viewModel::clearRecent,
                onProductClick = { id ->
                    viewModel.commitQuery()
                    onProductClick(id)
                },
                onAddToCart = addToCart,
                favorites = favorites
            )
        }
    }

    if (showFilters) {
        val content = (uiState as? UiState.Success)?.data
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            FilterSheet(
                initial = filters,
                content = content,
                onApply = {
                    viewModel.applyFilters(it)
                    showFilters = false
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            placeholder = { Text("Search products") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
        IconButton(onClick = onFilterClick) {
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondary) { Text(activeFilterCount.toString()) }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = if (activeFilterCount > 0) "Filters, $activeFilterCount active" else "Filters"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResults(
    content: SearchContent,
    query: String,
    recent: List<String>,
    onRecentClick: (String) -> Unit,
    onRecentRemove: (String) -> Unit,
    onRecentClear: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    favorites: FavoriteToggle
) {
    if (content.results.isEmpty()) {
        EmptyView(
            message = if (query.isBlank()) "No products match these filters" else "No results for \"$query\"",
            icon = Icons.Outlined.SearchOff
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (query.isBlank() && recent.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(horizontal = Spacing.sm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recent searches", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onRecentClear) { Text("Clear all") }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        recent.forEach { item ->
                            InputChip(
                                selected = false,
                                onClick = { onRecentClick(item) },
                                label = { Text(item) },
                                trailingIcon = {
                                    IconButton(onClick = { onRecentRemove(item) }, modifier = Modifier.height(18.dp)) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove $item",
                                            modifier = Modifier.height(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "${content.results.size} ${if (content.results.size == 1) "product" else "products"}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.sm)
            )
        }
        items(content.results, key = { it.id }) { product ->
            ProductItemView(
                product = product,
                onClick = { onProductClick(product.id) },
                onAddToCart = { onAddToCart(product.id) },
                isFavorite = favorites.isFavorite(product.id),
                onToggleFavorite = { favorites.toggle(product.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(
    initial: SearchFilters,
    content: SearchContent?,
    onApply: (SearchFilters) -> Unit
) {
    var categoryIds by remember { mutableStateOf(initial.categoryIds) }
    var sort by remember { mutableStateOf(initial.sort) }

    val maxRupees = ((content?.maxPricePaise ?: 0L) / 100).coerceAtLeast(1L).toFloat()
    var priceRange by remember {
        mutableStateOf(
            ((initial.minPricePaise ?: 0L) / 100f)..((initial.maxPricePaise?.div(100f)) ?: maxRupees)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            TextButton(onClick = { onApply(SearchFilters()) }) { Text("Reset") }
        }

        if (!content?.categories.isNullOrEmpty()) {
            Text("Category", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                content?.categories.orEmpty().forEach { category ->
                    ShoppFilterChip(
                        label = category.name,
                        selected = category.id in categoryIds,
                        onClick = {
                            categoryIds = if (category.id in categoryIds) categoryIds - category.id else categoryIds + category.id
                        }
                    )
                }
            }
        }

        Text("Price", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "${Money.format(priceRange.start.toLong() * 100)} - ${Money.format(priceRange.endInclusive.toLong() * 100)}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        RangeSlider(
            value = priceRange,
            onValueChange = { priceRange = it },
            valueRange = 0f..maxRupees
        )

        Text("Sort by", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SortOrder.entries.forEach { option ->
                ShoppFilterChip(
                    label = option.label,
                    selected = sort == option,
                    onClick = { sort = option }
                )
            }
        }

        Spacer(Modifier.height(Spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SecondaryButton(
                text = "Clear all",
                onClick = { onApply(SearchFilters()) },
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Show results",
                onClick = {
                    val coversAll = priceRange.start <= 0f && priceRange.endInclusive >= maxRupees
                    onApply(
                        SearchFilters(
                            categoryIds = categoryIds,
                            minPricePaise = if (coversAll || priceRange.start <= 0f) null else priceRange.start.toLong() * 100,
                            maxPricePaise = if (coversAll || priceRange.endInclusive >= maxRupees) null else priceRange.endInclusive.toLong() * 100,
                            sort = sort
                        )
                    )
                },
                modifier = Modifier.weight(1.4f)
            )
        }
        Spacer(Modifier.height(Spacing.md))
    }
}

@Composable
private fun ActiveFilters(
    filters: SearchFilters,
    categories: List<com.example.shopp.model.CategoryModel>,
    onChange: (SearchFilters) -> Unit
) {
    if (filters.activeCount == 0) return
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        filters.categoryIds.forEach { id ->
            item(key = "category-$id") {
                ActiveFilterChip(
                    label = categories.firstOrNull { it.id == id }?.name ?: id,
                    onRemove = { onChange(filters.copy(categoryIds = filters.categoryIds - id)) }
                )
            }
        }
        if (filters.minPricePaise != null || filters.maxPricePaise != null) {
            item(key = "price") {
                val low = Money.format(filters.minPricePaise ?: 0L)
                val label = if (filters.maxPricePaise != null) "$low - ${Money.format(filters.maxPricePaise)}" else "From $low"
                ActiveFilterChip(
                    label = label,
                    onRemove = { onChange(filters.copy(minPricePaise = null, maxPricePaise = null)) }
                )
            }
        }
        if (filters.sort != SortOrder.Relevance) {
            item(key = "sort") {
                ActiveFilterChip(
                    label = filters.sort.label,
                    onRemove = { onChange(filters.copy(sort = SortOrder.Relevance)) }
                )
            }
        }
    }
}
