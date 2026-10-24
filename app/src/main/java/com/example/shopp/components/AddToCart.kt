package com.example.shopp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shopp.ui.showToast
import com.example.shopp.viewmodel.CartActionsViewModel

/** Returns an add-to-cart action that gives haptic feedback and reports its result as a toast. */
@Composable
fun rememberAddToCart(viewModel: CartActionsViewModel = hiltViewModel()): (String) -> Unit {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { context.showToast(it) }
    }
    val haptics = LocalHapticFeedback.current
    return { productId ->
        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        viewModel.addToCart(productId)
    }
}
