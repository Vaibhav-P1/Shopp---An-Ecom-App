package com.example.shopp.seller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ShoppTextField
import com.example.shopp.model.SellerModel
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.SellerOrdersViewModel
import com.example.shopp.viewmodel.SellerProductEvent
import com.example.shopp.viewmodel.SellerProductsViewModel
import com.example.shopp.viewmodel.SellerSessionViewModel
import com.example.shopp.viewmodel.UiState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment

/**
 * Entry point of the seller area. Anyone can open it from Profile: users who are not sellers yet see the
 * sign-up form, sellers see their dashboard.
 */
@Composable
fun SellerScreen(
    modifier: Modifier = Modifier,
    onExit: () -> Unit,
    viewModel: SellerSessionViewModel = hiltViewModel()
) {
    val seller by viewModel.seller.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) { viewModel.messages.collect { context.showToast(it) } }

    when (val state = seller) {
        UiState.Loading -> ListSkeleton(modifier.padding(Spacing.lg), rows = 3)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> {
            val profile = state.data
            if (profile == null) {
                SellerOnboarding(modifier = modifier, onBack = onExit, viewModel = viewModel)
            } else {
                SellerShell(seller = profile, onExit = onExit, modifier = modifier)
            }
        }
    }
}

private enum class SellerTab(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Outlined.Dashboard),
    Products("Products", Icons.Outlined.Inventory2),
    Orders("Orders", Icons.Outlined.Receipt)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerShell(
    seller: SellerModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    productsViewModel: SellerProductsViewModel = hiltViewModel(),
    ordersViewModel: SellerOrdersViewModel = hiltViewModel()
) {
    var tab by rememberSaveable { mutableStateOf(SellerTab.Dashboard) }
    val products by productsViewModel.products.collectAsStateWithLifecycle()
    val orders by ordersViewModel.orders.collectAsStateWithLifecycle()
    val updating by ordersViewModel.updating.collectAsStateWithLifecycle()
    val categories by productsViewModel.categories.collectAsStateWithLifecycle()
    val editor by productsViewModel.editor.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(productsViewModel) {
        productsViewModel.events.collect { event ->
            when (event) {
                is SellerProductEvent.Message -> context.showToast(event.text)
                SellerProductEvent.Saved -> context.showToast("Product saved")
            }
        }
    }
    LaunchedEffect(ordersViewModel) {
        ordersViewModel.messages.collect { context.showToast(it) }
    }
    BackHandler(enabled = editor != null, onBack = productsViewModel::closeEditor)

    val editing = editor
    val ordersList = (orders as? UiState.Success)?.data.orEmpty()
    val productList = (products as? UiState.Success)?.data.orEmpty()
    val newOrders = newOrderCount(ordersList)
    Scaffold(
        modifier = modifier,
        topBar = {
            when {
                editing != null -> SellerHeader(
                    title = if (editing.productId == null) "Add product" else "Edit product",
                    subtitle = null,
                    backLabel = "Close editor",
                    onBack = productsViewModel::closeEditor
                )
                tab == SellerTab.Dashboard -> SellerHeader(
                    title = seller.shopName,
                    overline = "Seller dashboard",
                    subtitle = null,
                    backLabel = "Back to shopping",
                    onBack = onExit,
                    trailing = { ShopModePill() }
                )
                tab == SellerTab.Products -> SellerHeader(
                    title = "Products",
                    subtitle = productCountLabel(productList),
                    backLabel = "Back to shopping",
                    onBack = onExit
                )
                else -> SellerHeader(
                    title = "Orders",
                    subtitle = if (newOrders > 0) "$newOrders need your attention" else "All caught up",
                    backLabel = "Back to shopping",
                    onBack = onExit
                )
            }
        },
        bottomBar = {
            if (editing == null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    SellerTab.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = tab == entry,
                            onClick = { tab = entry },
                            icon = { Icon(entry.icon, contentDescription = null) },
                            label = { Text(entry.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val content = Modifier.padding(innerPadding)
        when {
            editing != null -> ProductEditor(
                state = editing,
                categories = categories,
                canUploadImages = productsViewModel.canUploadImages,
                onDraftChange = productsViewModel::updateDraft,
                onPickImages = productsViewModel::uploadImages,
                onSave = productsViewModel::save,
                modifier = content
            )
            tab == SellerTab.Dashboard -> SellerDashboard(
                shopName = seller.shopName,
                sellerId = ordersViewModel.sellerId,
                orders = orders,
                products = products,
                modifier = content
            )
            tab == SellerTab.Orders -> SellerOrdersPage(
                state = orders,
                sellerId = ordersViewModel.sellerId,
                updating = updating,
                onRetry = ordersViewModel::retry,
                onAdvance = ordersViewModel::advance,
                modifier = content
            )
            tab == SellerTab.Products -> SellerProductsPage(
                state = products,
                onRetry = productsViewModel::retry,
                onAdd = { productsViewModel.openEditor(null) },
                onEdit = productsViewModel::openEditor,
                onToggleActive = productsViewModel::setActive,
                onDelete = productsViewModel::delete,
                modifier = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerOnboarding(modifier: Modifier, onBack: () -> Unit, viewModel: SellerSessionViewModel) {
    val signup by viewModel.signup.collectAsStateWithLifecycle()
    var shopName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val submit = { viewModel.becomeSeller(shopName, phone) }

    Scaffold(
        modifier = modifier,
        topBar = { SellerHeader(title = "Become a seller", subtitle = null, backLabel = "Back", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = "List your products on Shopp and manage orders from your own dashboard.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            ShoppTextField(
                value = shopName,
                onValueChange = {
                    shopName = it
                    viewModel.clearError()
                },
                label = "Shop name",
                errorMessage = signup.errors.shopName
            )
            ShoppTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter(Char::isDigit).take(10)
                    viewModel.clearError()
                },
                label = "Contact number",
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submit() }),
                errorMessage = signup.errors.phone
            )
            Spacer(Modifier.height(Spacing.sm))
            PrimaryButton(text = "Create seller account", onClick = submit, isLoading = signup.isSaving)
        }
    }
}

/** Title row used at the top of every seller screen: back button, optional overline/subtitle and trailing content. */
@Composable
private fun SellerHeader(
    title: String,
    subtitle: String?,
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    overline: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backLabel)
        }
        Column(Modifier.weight(1f)) {
            if (overline != null) {
                Text(overline, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) {
            trailing()
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ShopModePill() {
    Text(
        text = "Shop mode",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
