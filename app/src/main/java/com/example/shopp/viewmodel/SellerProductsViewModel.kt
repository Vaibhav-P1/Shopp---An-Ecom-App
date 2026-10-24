package com.example.shopp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.CategoryRepository
import com.example.shopp.data.CloudinaryUploader
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

/** The product form while it is open: [productId] is null for a new product. */
data class ProductEditorState(
    val productId: String? = null,
    val draft: ProductDraft = ProductDraft(),
    val errors: ProductDraftErrors = ProductDraftErrors(),
    val isSaving: Boolean = false,
    val uploadingImages: Int = 0
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
    private val categoryRepository: CategoryRepository,
    private val uploader: CloudinaryUploader
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

    /** Null while the editor is closed. */
    private val _editor = MutableStateFlow<ProductEditorState?>(null)
    val editor: StateFlow<ProductEditorState?> = _editor

    /** Whether photos can be uploaded (Cloudinary configured); otherwise sellers paste image URLs. */
    val canUploadImages: Boolean get() = uploader.isConfigured

    private val _events = Channel<SellerProductEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { _categories.value = categoryRepository.getCategories().getOrDefault(emptyList()) }
    }

    fun retry() {
        reload.update { it + 1 }
    }

    /** Opens the form for [product], or a blank one when null. */
    fun openEditor(product: ProductModel?) {
        _editor.value = ProductEditorState(
            productId = product?.id,
            draft = product?.let {
                ProductDraft(
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    price = it.price,
                    actualPrice = it.actualPrice,
                    stock = it.stock?.toString().orEmpty(),
                    images = it.images,
                    active = it.active
                )
            } ?: ProductDraft()
        )
    }

    fun closeEditor() {
        _editor.value = null
    }

    fun updateDraft(transform: (ProductDraft) -> ProductDraft) {
        _editor.update { state -> state?.copy(draft = transform(state.draft)) }
    }

    fun uploadImages(uris: List<Uri>) {
        val room = ProductDraftValidator.MAX_IMAGES - (_editor.value?.draft?.images?.size ?: 0)
        uris.take(room.coerceAtLeast(0)).forEach { uri ->
            _editor.update { it?.copy(uploadingImages = it.uploadingImages + 1) }
            viewModelScope.launch {
                uploader.upload(uri).fold(
                    onSuccess = { url -> updateDraft { it.copy(images = it.images + url) } },
                    onFailure = { _events.send(SellerProductEvent.Message(it.localizedMessage ?: "Image upload failed")) }
                )
                _editor.update { it?.copy(uploadingImages = (it.uploadingImages - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun save() {
        val state = _editor.value ?: return
        if (state.isSaving || state.uploadingImages > 0) return
        val errors = ProductDraftValidator.validate(state.draft)
        _editor.value = state.copy(errors = errors)
        if (errors.hasErrors) return

        _editor.update { it?.copy(isSaving = true) }
        viewModelScope.launch {
            repository.save(state.productId, state.draft).fold(
                onSuccess = {
                    _editor.value = null
                    _events.send(SellerProductEvent.Saved)
                },
                onFailure = {
                    _editor.update { current -> current?.copy(isSaving = false) }
                    _events.send(SellerProductEvent.Message(it.localizedMessage ?: "Could not save the product"))
                }
            )
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
