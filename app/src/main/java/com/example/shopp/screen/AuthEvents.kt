package com.example.shopp.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.shopp.AppUtil
import com.example.shopp.viewmodel.AuthEvent
import com.example.shopp.viewmodel.AuthViewModel

/** Calls [onAuthenticated] on success and shows a toast on failure. */
@Composable
fun AuthEventsEffect(authViewModel: AuthViewModel, onAuthenticated: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(authViewModel) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.Authenticated -> onAuthenticated()
                is AuthEvent.Failure -> AppUtil.showToast(context, event.message)
            }
        }
    }
}
