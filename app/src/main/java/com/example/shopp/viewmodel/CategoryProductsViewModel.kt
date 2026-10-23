package com.example.shopp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shopp.CategoryProducts
import com.example.shopp.data.PageCursor
import com.example.shopp.data.ProductPage
import com.example.shopp.data.ProductRepository
import com.example.shopp.model.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryProductsContent(
    val products: List<ProductModel>,
    val isLoadingMore: Boolean = false,
    val loadMoreFailed: Boolean = false,
    val endReached: Boolean = false
)

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {

    private val categoryId: String = savedStateHandle.toRoute<CategoryProducts>().categoryId

    private val _uiState = MutableStateFlow<UiState<CategoryProductsContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<CategoryProductsContent>> = _uiState.asStateFlow()

    private var cursor: PageCursor? = null

    init {
        loadFirstPage()
    }

    fun retry() {
        _uiState.value = UiState.Loading
        loadFirstPage()
    }

    /** Appends the next page. Does nothing while a page is loading or once the last page is in. */
    fun loadMore() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        if (current.isLoadingMore || current.endReached) return

        _uiState.value = UiState.Success(current.copy(isLoadingMore = true, loadMoreFailed = false))
        viewModelScope.launch {
            repository.getProductsByCategory(categoryId, cursor).fold(
                onSuccess = { page ->
                    cursor = page.nextCursor
                    _uiState.value = UiState.Success(
                        CategoryProductsContent(
                            products = current.products + page.items,
                            endReached = page.endReached
                        )
                    )
                },
                onFailure = {
                    _uiState.value = UiState.Success(current.copy(isLoadingMore = false, loadMoreFailed = true))
                }
            )
        }
    }

    private fun loadFirstPage() {
        cursor = null
        viewModelScope.launch {
            _uiState.value = repository.getProductsByCategory(categoryId).fold(
                onSuccess = { page: ProductPage ->
                    cursor = page.nextCursor
                    UiState.Success(CategoryProductsContent(page.items, endReached = page.endReached))
                },
                onFailure = { UiState.Error(it.localizedMessage ?: "Something went wrong") }
            )
        }
    }
}
