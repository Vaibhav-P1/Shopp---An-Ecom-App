package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.EmptyView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.ListSkeleton
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.components.ShoppTextField
import com.example.shopp.domain.AddressValidator
import com.example.shopp.model.AddressModel
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.AddressBookViewModel
import com.example.shopp.viewmodel.AddressEvent
import com.example.shopp.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBookPage(
    modifier: Modifier = Modifier,
    viewModel: AddressBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // null = form closed, an empty address = adding, otherwise editing that address
    var editing by remember { mutableStateOf<AddressModel?>(null) }
    var pendingDelete by remember { mutableStateOf<AddressModel?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AddressEvent.Message -> context.showToast(event.text)
                AddressEvent.Saved -> editing = null
            }
        }
    }

    Column(modifier.fillMaxSize().padding(Spacing.lg)) {
        Text("Saved addresses", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(Spacing.lg))

        when (val state = uiState) {
            UiState.Loading -> ListSkeleton(Modifier.weight(1f), rows = 3)
            is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = Modifier.weight(1f))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyView(
                        message = "No saved addresses yet",
                        icon = Icons.Outlined.LocationOn,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(state.data, key = { it.id }) { address ->
                            AddressCard(
                                address = address,
                                onMakePrimary = { viewModel.setPrimary(address.id) },
                                onEdit = { editing = address },
                                onDelete = { pendingDelete = address }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                PrimaryButton(text = "Add new address", onClick = { editing = AddressModel() })
            }
        }
    }

    editing?.let { address ->
        ModalBottomSheet(
            onDismissRequest = { editing = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddressForm(initial = address, onSave = viewModel::save)
        }
    }

    pendingDelete?.let { address ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete address?") },
            text = { Text("\"${address.label}\" will be removed from your saved addresses.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(address.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddressCard(
    address: AddressModel,
    onMakePrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ShoppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(address.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (address.primary) {
                    AssistChip(onClick = {}, label = { Text("Default") }, enabled = false)
                }
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(address.formatted, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!address.primary) {
                    TextButton(onClick = onMakePrimary) { Text("Make default") }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${address.label}")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${address.label}")
                }
            }
        }
    }
}

@Composable
private fun AddressForm(initial: AddressModel, onSave: (AddressModel) -> Unit) {
    var label by remember { mutableStateOf(initial.label) }
    var recipient by remember { mutableStateOf(initial.recipient) }
    var phone by remember { mutableStateOf(initial.phone) }
    var street by remember { mutableStateOf(initial.street) }
    var city by remember { mutableStateOf(initial.city) }
    var pincode by remember { mutableStateOf(initial.pincode) }
    var showErrors by remember { mutableStateOf(false) }

    val errors = AddressValidator.validate(label, recipient, phone, street, city, pincode)
    fun submit() {
        showErrors = true
        if (!errors.hasErrors) {
            onSave(initial.copy(label = label, recipient = recipient, phone = phone, street = street, city = city, pincode = pincode))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = if (initial.id.isBlank()) "Add address" else "Edit address",
            style = MaterialTheme.typography.titleLarge
        )
        ShoppTextField(label, { label = it }, "Name (Home, Work...)", errorMessage = errors.label.takeIf { showErrors })
        ShoppTextField(recipient, { recipient = it }, "Recipient name", errorMessage = errors.recipient.takeIf { showErrors })
        ShoppTextField(
            phone, { phone = it.filter(Char::isDigit).take(10) }, "Mobile number",
            keyboardType = KeyboardType.Phone, errorMessage = errors.phone.takeIf { showErrors }
        )
        ShoppTextField(street, { street = it }, "House no, street, area", errorMessage = errors.street.takeIf { showErrors })
        ShoppTextField(city, { city = it }, "City", errorMessage = errors.city.takeIf { showErrors })
        ShoppTextField(
            pincode, { pincode = it.filter(Char::isDigit).take(6) }, "Pincode",
            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { submit() }),
            errorMessage = errors.pincode.takeIf { showErrors }
        )
        Spacer(Modifier.height(Spacing.xs))
        PrimaryButton(text = "Save address", onClick = ::submit)
        Spacer(Modifier.height(Spacing.md))
    }
}
