package com.example.shopp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shopp.CategoryProducts
import com.example.shopp.data.ProductRepository
import com.example.shopp.model.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {

    private val categoryId: String = savedStateHandle.toRoute<CategoryProducts>().categoryId

    private val _uiState = MutableStateFlow<UiState<List<ProductModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ProductModel>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _uiState.value = UiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = repository.getProductsByCategory(categoryId).fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.localizedMessage ?: "Something went wrong") }
            )
        }
    }
}
