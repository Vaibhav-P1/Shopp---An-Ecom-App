package com.example.shopp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.R
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.ShoppTextField
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.AuthViewModel

@Composable
fun SignupScreen(modifier: Modifier = Modifier, onAuthenticated: () -> Unit, authViewModel: AuthViewModel = hiltViewModel()) {

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.isLoading
    AuthEventsEffect(authViewModel, onAuthenticated)

    AuthLayout(modifier) {
        Text(
            text = "Hello there!",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "Create an account",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Image(
            painter = painterResource(id = R.drawable.signupbanner),
            contentDescription = "Banner",
            modifier = Modifier.fillMaxWidth().height(220.dp)
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        ShoppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        ShoppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name"
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        ShoppTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { authViewModel.signup(email, name, password) })
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        PrimaryButton(
            text = "Signup",
            onClick = { authViewModel.signup(email, name, password) },
            isLoading = isLoading
        )
    }
}
