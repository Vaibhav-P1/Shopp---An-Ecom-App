package com.example.shopp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.R
import com.example.shopp.components.BrandLogo
import com.example.shopp.components.LabeledDivider
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.RoundBackButton
import com.example.shopp.components.ShoppTextField
import com.example.shopp.components.SwitchAuthPrompt
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.AuthField
import com.example.shopp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSignupClick: () -> Unit,
    onAuthenticated: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val webClientId = context.getString(R.string.default_web_client_id)

    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.isLoading
    AuthEventsEffect(authViewModel, onAuthenticated)

    AuthLayout(modifier) {
        RoundBackButton(onClick = onBack)

        Spacer(Modifier.height(Spacing.xl))
        BrandLogo(size = 44.dp)
        Spacer(Modifier.height(18.dp))

        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Log in to pick up where you left off.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(Spacing.xxl))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            ShoppTextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.clearError(AuthField.Email)
                },
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                errorMessage = uiState.emailError
            )
            ShoppTextField(
                value = password,
                onValueChange = {
                    password = it
                    authViewModel.clearError(AuthField.Password)
                },
                label = "Password",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                errorMessage = uiState.passwordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { authViewModel.login(email, password) })
            )
        }

        Spacer(Modifier.height(28.dp))

        PrimaryButton(
            text = "Log in",
            onClick = { authViewModel.login(email, password) },
            isLoading = isLoading
        )

        LabeledDivider("or continue with", Modifier.padding(vertical = 22.dp))

        GoogleButton(
            onClick = { authViewModel.googleSignIn(context, scope, webClientId) },
            enabled = !isLoading
        )

        Spacer(Modifier.weight(1f))
        SwitchAuthPrompt(
            prompt = "New to Shopp?",
            action = "Create an account",
            onClick = onSignupClick,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Spacing.lg)
        )
    }
}
