package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.SellerRepository
import com.example.shopp.domain.SellerFormErrors
import com.example.shopp.domain.SellerValidator
import com.example.shopp.model.SellerModel
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

data class SellerSignupState(
    val errors: SellerFormErrors = SellerFormErrors(),
    val isSaving: Boolean = false
)

/** Tells the seller area whether the signed-in user is already a seller (Success(profile)) or not (Success(null)). */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SellerSessionViewModel @Inject constructor(
    private val repository: SellerRepository
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    val seller: StateFlow<UiState<SellerModel?>> = reload
        .flatMapLatest {
            repository.observeSeller()
                .map<SellerModel?, UiState<SellerModel?>> { UiState.Success(it) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _signup = MutableStateFlow(SellerSignupState())
    val signup: StateFlow<SellerSignupState> = _signup

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun retry() {
        reload.update { it + 1 }
    }

    fun becomeSeller(shopName: String, phone: String) {
        if (_signup.value.isSaving) return
        val errors = SellerValidator.validate(shopName, phone)
        _signup.value = SellerSignupState(errors = errors)
        if (errors.hasErrors) return

        _signup.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.becomeSeller(shopName, phone).onFailure {
                _messages.send(it.localizedMessage ?: "Could not create your seller account")
            }
            // On success the live seller document flips the screen to the dashboard
            _signup.update { it.copy(isSaving = false) }
        }
    }

    fun clearError() {
        _signup.update { it.copy(errors = SellerFormErrors()) }
    }
}
