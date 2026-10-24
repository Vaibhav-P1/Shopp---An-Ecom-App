package com.example.shopp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopp.data.CategoryRepository
import com.example.shopp.data.ProductRepository
import com.example.shopp.data.RecentSearchesRepository
import com.example.shopp.domain.ProductSearch
import com.example.shopp.domain.SearchFilters
import com.example.shopp.model.CategoryModel
import com.example.shopp.model.ProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Everything the search screen needs once the catalogue has loaded. */
data class SearchContent(
    val categories: List<CategoryModel>,
    val maxPricePaise: Long,
    val results: List<ProductModel>
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val recentSearches: RecentSearchesRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters

    val recent: StateFlow<List<String>> = recentSearches.searches

    private val catalogue = MutableStateFlow<UiState<Pair<List<ProductModel>, List<CategoryModel>>>>(UiState.Loading)

    /** The query is debounced so typing does not re-filter on every keystroke. */
    private val debouncedQuery = _query.debounce(QUERY_DEBOUNCE_MS).distinctUntilChanged()

    val uiState: StateFlow<UiState<SearchContent>> = combine(catalogue, debouncedQuery, _filters) { state, query, filters ->
        when (state) {
            UiState.Loading -> UiState.Loading
            is UiState.Error -> state
            is UiState.Success -> {
                val (products, categories) = state.data
                UiState.Success(
                    SearchContent(
                        categories = categories,
                        maxPricePaise = ProductSearch.maxPricePaise(products),
                        results = ProductSearch.search(products, query, filters)
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        loadCatalogue()
    }

    fun retry() {
        catalogue.value = UiState.Loading
        loadCatalogue(forceRefresh = true)
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Called when the user submits the keyboard search action or picks a result. */
    fun commitQuery() = recentSearches.add(_query.value)

    fun useRecent(query: String) {
        _query.value = query
        recentSearches.add(query)
    }

    fun removeRecent(query: String) = recentSearches.remove(query)

    fun clearRecent() = recentSearches.clear()

    fun applyFilters(filters: SearchFilters) {
        _filters.update { filters }
    }

    private fun loadCatalogue(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val products = productRepository.getAllProducts(forceRefresh)
            // Categories only add filter chips, so a failure there must not break search
            val categories = categoryRepository.getCategories().getOrDefault(emptyList())
            catalogue.value = products.fold(
                onSuccess = { UiState.Success(it to categories) },
                onFailure = { UiState.Error(it.localizedMessage ?: "Something went wrong") }
            )
        }
    }

    private companion object {
        const val QUERY_DEBOUNCE_MS = 250L
    }
}
