package com.example.shopp.seller

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
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerShell(seller: SellerModel, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(seller.shopName) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to shopping")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(Spacing.lg)) {
            Text("Welcome, ${seller.shopName}", style = MaterialTheme.typography.titleLarge)
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
