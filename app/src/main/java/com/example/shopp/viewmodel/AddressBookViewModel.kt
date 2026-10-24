package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.AddressRepository
import com.example.shopp.model.AddressModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed interface AddressEvent {
    data class Message(val text: String) : AddressEvent
    /** The address was stored, so the form can close. */
    data object Saved : AddressEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddressBookViewModel @Inject constructor(
    private val repository: AddressRepository
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    val uiState: StateFlow<UiState<List<AddressModel>>> = reload
        .flatMapLatest {
            repository.observeAddresses()
                .map<List<AddressModel>, UiState<List<AddressModel>>> { UiState.Success(it) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Something went wrong")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _events = Channel<AddressEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun retry() {
        reload.value++
    }

    fun save(address: AddressModel) = launchAction("Could not save the address") {
        repository.save(address).onSuccess { _events.send(AddressEvent.Saved) }
    }

    fun setPrimary(addressId: String) = launchAction("Could not change the primary address") {
        repository.setPrimary(addressId)
    }

    fun delete(addressId: String) = launchAction("Could not delete the address") {
        repository.delete(addressId)
    }

    private fun launchAction(failureMessage: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            block().onFailure { _events.send(AddressEvent.Message(it.localizedMessage ?: failureMessage)) }
        }
    }
}
