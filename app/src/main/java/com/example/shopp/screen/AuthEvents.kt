package com.example.shopp.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.shopp.AppUtil
import com.example.shopp.viewmodel.AuthEvent
import com.example.shopp.viewmodel.AuthViewModel

/** Navigates home on success and shows a toast on failure. */
@Composable
fun AuthEventsEffect(authViewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(authViewModel) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.Authenticated -> navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
                is AuthEvent.Failure -> AppUtil.showToast(context, event.message)
            }
        }
    }
}
