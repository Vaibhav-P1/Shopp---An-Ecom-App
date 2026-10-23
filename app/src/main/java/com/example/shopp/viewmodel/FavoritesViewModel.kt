package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.FavoritesRepository
import com.example.shopp.data.ProductRepository
import com.example.shopp.model.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productCache = mutableMapOf<String, ProductModel>()
    private val reload = MutableStateFlow(0)

    val uiState: StateFlow<UiState<List<ProductModel>>> = reload
        .flatMapLatest {
            favoritesRepository.observeFavorites()
                .map<Set<String>, UiState<List<ProductModel>>> { ids -> UiState.Success(toProducts(ids)) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() {
        reload.value++
    }

    private suspend fun toProducts(ids: Set<String>): List<ProductModel> {
        val missing = ids - productCache.keys
        if (missing.isNotEmpty()) {
            productRepository.getProductsByIds(missing).getOrThrow().forEach { productCache[it.id] = it }
        }
        // Products deleted from the catalogue simply drop out of the list
        return ids.mapNotNull { productCache[it] }.sortedBy { it.title }
    }
}
