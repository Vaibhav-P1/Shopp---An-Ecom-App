package com.example.shopp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.ui.showToast
import com.example.shopp.R
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ShoppCard
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.ProfileData
import com.example.shopp.viewmodel.ProfileEvent
import com.example.shopp.viewmodel.ProfileViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    onOrdersClick: () -> Unit,
    onSellerClick: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.Message -> context.showToast(event.text)
                ProfileEvent.SignedOut -> onSignedOut()
            }
        }
    }

    when (val state = uiState) {
        UiState.Loading -> LoadingView(modifier)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> ProfileContent(
            data = state.data,
            onSaveAddress = viewModel::saveAddress,
            onOrdersClick = onOrdersClick,
            onSellerClick = onSellerClick,
            onSignOut = viewModel::signOut,
            modifier = modifier
        )
    }
}

@Composable
private fun ProfileContent(
    data: ProfileData,
    onSaveAddress: (String) -> Unit,
    onOrdersClick: () -> Unit,
    onSellerClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addressInput by remember(data.user.address) { mutableStateOf(data.user.address) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile Icon",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = data.user.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = addressInput,
            onValueChange = { addressInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter delivery address") },
            placeholder = { Text("House No, Street, City, State") },
            trailingIcon = {
                IconButton(onClick = { onSaveAddress(addressInput) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Address"
                    )
                }
            },
            singleLine = false,
            minLines = 2,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSaveAddress(addressInput) })
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Email:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(text = data.user.email, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Number of items in cart", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(text = "${data.cartItemCount} items", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(20.dp))

        ShoppCard(
            onClick = onOrdersClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Orders",
                    fontWeight = FontWeight.SemiBold
                )

                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ShoppCard(
            onClick = onSellerClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Create Seller Account",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        PrimaryButton(text = "Sign Out", onClick = onSignOut)
    }
}
