package com.example.shopp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType

@Composable
fun BannerView(banners: List<String>, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
    ) {
        val pagerState = rememberPagerState(0) { banners.size }
        HorizontalPager(
            state = pagerState,
            pageSpacing = 24.dp,
        ) {
            AsyncImage(
                model = banners[it],
                contentDescription = "Banner Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))

            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        DotsIndicator(
            dotCount = banners.size,
            type = ShiftIndicatorType(DotGraphic(
                color = MaterialTheme.colorScheme.primary,
                size = 6.dp
            )),
            pagerState = pagerState
        )

    }
}
