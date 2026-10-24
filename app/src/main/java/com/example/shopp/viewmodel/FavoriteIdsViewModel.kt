package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The signed-in user's favourite product ids, so product cards can show and flip the heart. */
@HiltViewModel
class FavoriteIdsViewModel @Inject constructor(
    private val repository: FavoritesRepository
) : ViewModel() {

    val ids: StateFlow<Set<String>> = repository.observeFavorites()
        .catch { emit(emptySet()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggle(productId: String) {
        val makeFavorite = productId !in ids.value
        viewModelScope.launch { repository.setFavorite(productId, makeFavorite) }
    }
}
