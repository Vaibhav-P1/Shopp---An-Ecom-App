package com.example.shopp.seller

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.shopp.model.ProductModel
import com.example.shopp.model.SellerModel
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
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
    Products("Products", Icons.Outlined.Inventory2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerShell(
    seller: SellerModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    productsViewModel: SellerProductsViewModel = hiltViewModel()
) {
    var tab by rememberSaveable { mutableStateOf(SellerTab.Dashboard) }
    // null = editor closed; otherwise the product being edited (isNew means a blank one)
    var editing by remember { mutableStateOf<EditorTarget?>(null) }
    val products by productsViewModel.products.collectAsStateWithLifecycle()
    val categories by productsViewModel.categories.collectAsStateWithLifecycle()
    val editor by productsViewModel.editor.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(productsViewModel) {
        productsViewModel.events.collect { event ->
            when (event) {
                is SellerProductEvent.Message -> context.showToast(event.text)
                SellerProductEvent.Saved -> {
                    editing = null
                    context.showToast("Product saved")
                }
            }
        }
    }
    BackHandler(enabled = editing != null) {
        editing = null
        productsViewModel.resetEditor()
    }

    val target = editing
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            target == null -> seller.shopName
                            target.product == null -> "Add product"
                            else -> "Edit product"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (target == null) {
                            onExit()
                        } else {
                            editing = null
                            productsViewModel.resetEditor()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (target == null) "Back to shopping" else "Close editor"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (target == null) {
                NavigationBar {
                    SellerTab.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = tab == entry,
                            onClick = { tab = entry },
                            icon = { Icon(entry.icon, contentDescription = null) },
                            label = { Text(entry.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val content = Modifier.padding(innerPadding)
        when {
            target != null -> ProductEditor(
                product = target.product,
                categories = categories,
                state = editor,
                onSave = { draft -> productsViewModel.save(target.product?.id, draft) },
                modifier = content
            )
            tab == SellerTab.Dashboard -> Column(content.padding(Spacing.lg)) {
                Text("Welcome, ${seller.shopName}", style = MaterialTheme.typography.titleLarge)
            }
            tab == SellerTab.Products -> SellerProductsPage(
                state = products,
                onRetry = productsViewModel::retry,
                onAdd = { editing = EditorTarget(null) },
                onEdit = { editing = EditorTarget(it) },
                onToggleActive = productsViewModel::setActive,
                onDelete = productsViewModel::delete,
                modifier = content
            )
        }
    }
}

/** What the product editor is showing: a blank form when [product] is null, otherwise that product. */
private data class EditorTarget(val product: ProductModel?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerOnboarding(modifier: Modifier, onBack: () -> Unit, viewModel: SellerSessionViewModel) {
    val signup by viewModel.signup.collectAsStateWithLifecycle()
    var shopName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val submit = { viewModel.becomeSeller(shopName, phone) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Become a seller") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                modifier = Modifier.height(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
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
