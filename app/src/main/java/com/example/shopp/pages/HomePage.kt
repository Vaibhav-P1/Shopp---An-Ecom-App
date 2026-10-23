package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.BannerView
import com.example.shopp.components.CategoriesView
import com.example.shopp.components.HeaderView
import com.example.shopp.components.OfferCard
import com.example.shopp.viewmodel.HomeUiState
import com.example.shopp.viewmodel.HomeViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()

    when (val state = uiState) {
        HomeUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is HomeUiState.Error -> Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = state.message)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = homeViewModel::retry) { Text("Retry") }
        }

        is HomeUiState.Success -> HomeContent(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = homeViewModel::refresh,
            onCategoryClick = onCategoryClick,
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
                .padding(horizontal = 16.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Search screen arrives with Phase B
                HeaderView(name = state.userName, onSearchClick = {})
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                BannerView(banners = state.banners, modifier = Modifier.height(170.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(text = "Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                CategoriesView(categories = state.categories, onCategoryClick = onCategoryClick)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(text = "🔥 Deals & Offers", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(state.offers, key = { it.id }) { offer ->
                OfferCard(offer = offer, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
