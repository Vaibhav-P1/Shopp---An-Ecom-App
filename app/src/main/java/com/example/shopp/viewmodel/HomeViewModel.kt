package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.BannerRepository
import com.example.shopp.data.CategoryRepository
import com.example.shopp.data.OfferRepository
import com.example.shopp.data.UserRepository
import com.example.shopp.model.CategoryModel
import com.example.shopp.model.OfferModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val userName: String,
        val banners: List<String>,
        val categories: List<CategoryModel>,
        val offers: List<OfferModel>
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bannerRepository: BannerRepository,
    private val categoryRepository: CategoryRepository,
    private val offerRepository: OfferRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        _isRefreshing.value = true
        load()
    }

    fun retry() {
        _uiState.value = HomeUiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val user = async { userRepository.getFirstName() }
            val banners = async { bannerRepository.getBannerUrls() }
            val categories = async { categoryRepository.getCategories() }
            val offers = async { offerRepository.getOffers() }

            val results = listOf(user.await(), banners.await(), categories.await(), offers.await())
            val state = if (results.all { it.isFailure }) {
                HomeUiState.Error(results.first().exceptionOrNull()?.localizedMessage ?: "Something went wrong")
            } else {
                // A section that fails to load is shown empty rather than blocking the whole page
                HomeUiState.Success(
                    userName = user.await().getOrDefault(""),
                    banners = banners.await().getOrDefault(emptyList()),
                    categories = categories.await().getOrDefault(emptyList()),
                    offers = offers.await().getOrDefault(emptyList())
                )
            }
            _uiState.update { state }
            _isRefreshing.value = false
        }
    }
}
