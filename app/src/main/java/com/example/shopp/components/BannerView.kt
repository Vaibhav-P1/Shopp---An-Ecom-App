package com.example.shopp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

private const val BANNER_INTERVAL_MS = 4_000L

@Composable
fun BannerView(banners: List<String>, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
    ) {
        val pagerState = rememberPagerState(0) { banners.size }
        val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
        // Auto-advance every few seconds; the timer restarts whenever the user touches the pager
        LaunchedEffect(pagerState, banners.size, isDragged) {
            if (banners.size < 2 || isDragged) return@LaunchedEffect
            while (true) {
                delay(BANNER_INTERVAL_MS)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
            }
        }
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = banners[it],
                contentDescription = "Promotion ${it + 1} of ${banners.size}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(26.dp))

            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(banners.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    Modifier
                        .height(6.dp)
                        .width(if (selected) 20.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}
