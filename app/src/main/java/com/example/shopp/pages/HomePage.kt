package com.example.shopp.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.BannerView
import com.example.shopp.components.CategoriesView
import com.example.shopp.components.ErrorView
import com.example.shopp.components.HeaderView
import com.example.shopp.components.HomeSkeleton
import com.example.shopp.components.OfferCard
import com.example.shopp.components.SearchPill
import com.example.shopp.components.SectionHeader
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.HomeUiState
import com.example.shopp.viewmodel.HomeViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()

    when (val state = uiState) {
        HomeUiState.Loading -> HomeSkeleton(modifier)

        is HomeUiState.Error -> ErrorView(state.message, onRetry = homeViewModel::retry, modifier = modifier)

        is HomeUiState.Success -> HomeContent(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = homeViewModel::refresh,
            onCategoryClick = onCategoryClick,
            onSearchClick = onSearchClick,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                HeaderView(name = state.userName)
                Spacer(modifier = Modifier.height(Spacing.lg))
                SearchPill(onClick = onSearchClick)
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            item {
                BannerView(banners = state.banners, modifier = Modifier.height(190.dp))
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            item {
                SectionHeader("Categories")
                CategoriesView(categories = state.categories, onCategoryClick = onCategoryClick)
                Spacer(modifier = Modifier.height(Spacing.xl))
            }

            item {
                SectionHeader("Deals for you")
            }

            items(state.offers, key = { it.id }) { offer ->
                OfferCard(offer = offer, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { Spacer(Modifier.height(Spacing.lg)) }
        }
    }
}
