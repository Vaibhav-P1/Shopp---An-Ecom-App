package com.example.shopp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shopp.ProductDetails
import com.example.shopp.data.FavoritesRepository
import com.example.shopp.data.ProductRepository
import com.example.shopp.model.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val productId: String = savedStateHandle.toRoute<ProductDetails>().productId

    private val _uiState = MutableStateFlow<UiState<ProductModel>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProductModel>> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favoritesRepository.observeFavorites()
        .map { productId in it }
        .catch { emit(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _similar = MutableStateFlow<List<ProductModel>>(emptyList())

    /** Other products from the same category, shown under the details. */
    val similar: StateFlow<List<ProductModel>> = _similar.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        load()
    }

    fun toggleFavorite() {
        val makeFavorite = !isFavorite.value
        viewModelScope.launch {
            favoritesRepository.setFavorite(productId, makeFavorite).fold(
                onSuccess = {
                    _messages.send(if (makeFavorite) "Item added to favorite" else "Item removed from favorite")
                },
                onFailure = { _messages.send(it.localizedMessage ?: "Could not update favorites") }
            )
        }
    }

    fun retry() {
        _uiState.value = UiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = repository.getProduct(productId).fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.localizedMessage ?: "Something went wrong") }
            )
            (_uiState.value as? UiState.Success)?.data?.let { product ->
                // Suggestions are a bonus: if the catalogue cannot be loaded the section is simply hidden
                _similar.value = repository.getAllProducts().getOrDefault(emptyList())
                    .filter { it.category == product.category && it.id != product.id }
                    .take(SIMILAR_LIMIT)
            }
        }
    }

    private companion object {
        const val SIMILAR_LIMIT = 8
    }
}
