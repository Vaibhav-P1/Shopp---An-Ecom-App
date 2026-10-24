package com.example.shopp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.viewmodel.FavoriteIdsViewModel

/** Favourite state for a list of product cards: the current ids and a function that flips one. */
class FavoriteToggle(val ids: Set<String>, val toggle: (String) -> Unit) {
    fun isFavorite(productId: String) = productId in ids
}

@Composable
fun rememberFavoriteToggle(viewModel: FavoriteIdsViewModel = hiltViewModel()): FavoriteToggle {
    val ids by viewModel.ids.collectAsStateWithLifecycle()
    return FavoriteToggle(ids, viewModel::toggle)
}
