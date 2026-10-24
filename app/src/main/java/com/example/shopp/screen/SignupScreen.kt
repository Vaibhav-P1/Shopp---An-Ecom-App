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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.PasswordStrengthMeter
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.RoundBackButton
import com.example.shopp.components.ShoppTextField
import com.example.shopp.components.SwitchAuthPrompt
import com.example.shopp.domain.PasswordStrength
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.AuthField
import com.example.shopp.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    onAuthenticated: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.isLoading
    AuthEventsEffect(authViewModel, onAuthenticated)

    AuthLayout(modifier) {
        RoundBackButton(onClick = onBack)

        Spacer(Modifier.height(Spacing.xl))
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "It takes less than a minute.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(Spacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            ShoppTextField(
                value = name,
                onValueChange = {
                    name = it
                    authViewModel.clearError(AuthField.Name)
                },
                label = "Full name",
                leadingIcon = Icons.Outlined.Person,
                errorMessage = uiState.nameError
            )
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
                keyboardActions = KeyboardActions(onDone = { authViewModel.signup(email, name, password) })
            )
            PasswordStrengthMeter(PasswordStrength.of(password))
        }

        Spacer(Modifier.height(28.dp))

        PrimaryButton(
            text = "Create account",
            onClick = { authViewModel.signup(email, name, password) },
            isLoading = isLoading
        )
        Text(
            text = "By signing up you agree to our Terms of Service and Privacy Policy.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
        )

        Spacer(Modifier.weight(1f))
        SwitchAuthPrompt(
            prompt = "Already have an account?",
            action = "Log in",
            onClick = onLoginClick,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Spacing.lg)
        )
    }
}
