package com.example.shopp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shopp.ProductDetails
import com.example.shopp.data.AuthRepository
import com.example.shopp.data.OrderRepository
import com.example.shopp.data.ReviewRepository
import com.example.shopp.domain.ReviewSummary
import com.example.shopp.model.OrderStatus
import com.example.shopp.model.ReviewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewsUiState(
    val reviews: List<ReviewModel> = emptyList(),
    val summary: ReviewSummary = ReviewSummary.Empty,
    /** The signed-in user's own review of this product, if any. */
    val myReview: ReviewModel? = null,
    /** A delivered order containing this product, which entitles the user to review it; null otherwise. */
    val reviewOrderId: String? = null
)

@HiltViewModel
class ProductReviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository,
    orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val productId: String = savedStateHandle.toRoute<ProductDetails>().productId

    val uiState: StateFlow<ReviewsUiState> = combine(
        reviewRepository.observeReviews(productId).catch { emit(emptyList()) },
        orderRepository.observeOrders().map { orders ->
            orders.firstOrNull { it.status == OrderStatus.DELIVERED && productId in it.items.keys }?.id
        }.catch { emit(null) }
    ) { reviews, orderId ->
        val uid = authRepository.currentUserId
        ReviewsUiState(
            reviews = reviews,
            summary = ReviewSummary.of(reviews.map { it.rating }),
            myReview = reviews.firstOrNull { it.userId == uid },
            reviewOrderId = orderId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReviewsUiState())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun submit(rating: Int, text: String) {
        val orderId = uiState.value.reviewOrderId ?: return
        if (_isSaving.value || rating !in 1..5) return
        _isSaving.value = true
        viewModelScope.launch {
            reviewRepository.submit(productId, orderId, rating, text).fold(
                onSuccess = { _messages.send("Thanks for your review") },
                onFailure = { _messages.send(it.localizedMessage ?: "Could not save your review") }
            )
            _isSaving.value = false
        }
    }

    fun deleteMine() {
        viewModelScope.launch {
            reviewRepository.delete(productId).onFailure {
                _messages.send(it.localizedMessage ?: "Could not delete your review")
            }
        }
    }
}
