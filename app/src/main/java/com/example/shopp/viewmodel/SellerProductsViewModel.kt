package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.CategoryRepository
import com.example.shopp.data.SellerProductRepository
import com.example.shopp.domain.ProductDraft
import com.example.shopp.domain.ProductDraftErrors
import com.example.shopp.domain.ProductDraftValidator
import com.example.shopp.model.CategoryModel
import com.example.shopp.model.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductEditorState(
    val errors: ProductDraftErrors = ProductDraftErrors(),
    val isSaving: Boolean = false
)

sealed interface SellerProductEvent {
    data class Message(val text: String) : SellerProductEvent
    /** The product was stored, so the editor can close. */
    data object Saved : SellerProductEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SellerProductsViewModel @Inject constructor(
    private val repository: SellerProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    val products: StateFlow<UiState<List<ProductModel>>> = reload
        .flatMapLatest {
            repository.observeMyProducts()
                .map<List<ProductModel>, UiState<List<ProductModel>>> { UiState.Success(it) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _categories = MutableStateFlow<List<CategoryModel>>(emptyList())
    val categories: StateFlow<List<CategoryModel>> = _categories

    private val _editor = MutableStateFlow(ProductEditorState())
    val editor: StateFlow<ProductEditorState> = _editor

    private val _events = Channel<SellerProductEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { _categories.value = categoryRepository.getCategories().getOrDefault(emptyList()) }
    }

    fun retry() {
        reload.update { it + 1 }
    }

    fun resetEditor() {
        _editor.value = ProductEditorState()
    }

    fun save(productId: String?, draft: ProductDraft) {
        if (_editor.value.isSaving) return
        val errors = ProductDraftValidator.validate(draft)
        _editor.value = ProductEditorState(errors = errors)
        if (errors.hasErrors) return

        _editor.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.save(productId, draft).fold(
                onSuccess = { _events.send(SellerProductEvent.Saved) },
                onFailure = { _events.send(SellerProductEvent.Message(it.localizedMessage ?: "Could not save the product")) }
            )
            _editor.update { it.copy(isSaving = false) }
        }
    }

    fun setActive(product: ProductModel, active: Boolean) = launchAction("Could not update the product") {
        repository.setActive(product.id, active)
    }

    fun delete(product: ProductModel) = launchAction("Could not delete the product") {
        repository.delete(product.id)
    }

    private fun launchAction(failureMessage: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            block().onFailure { _events.send(SellerProductEvent.Message(it.localizedMessage ?: failureMessage)) }
        }
    }
}
