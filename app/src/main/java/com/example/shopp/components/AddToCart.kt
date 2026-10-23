package com.example.shopp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shopp.ui.showToast
import com.example.shopp.viewmodel.CartActionsViewModel

/** Returns an add-to-cart action that reports its result as a toast. */
@Composable
fun rememberAddToCart(viewModel: CartActionsViewModel = hiltViewModel()): (String) -> Unit {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { context.showToast(it) }
    }
    return viewModel::addToCart
}
