package com.example.shopp.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/** Animated placeholder fill that sweeps a highlight across the element. */
fun Modifier.shimmer(): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = MaterialTheme.colorScheme.surfaceContainerLowest
    val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    val travel = 1200f
    val start = -travel + progress * travel * 2
    background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(start, 0f),
            end = Offset(start + travel, travel / 2)
        )
    )
}

@Composable
fun ShimmerBlock(modifier: Modifier = Modifier, cornerRadius: Int = 12) {
    Box(modifier.clip(RoundedCornerShape(cornerRadius.dp)).shimmer())
}

/** Skeleton of the Home page: header, banner, category row and an offer card. */
@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).clearAndSetSemantics { },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBlock(Modifier.width(180.dp).height(28.dp))
        ShimmerBlock(Modifier.fillMaxWidth().height(170.dp), cornerRadius = 20)
        ShimmerBlock(Modifier.width(120.dp).height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { ShimmerBlock(Modifier.size(100.dp), cornerRadius = 16) }
        }
        ShimmerBlock(Modifier.fillMaxWidth().height(170.dp), cornerRadius = 20)
    }
}

/** Skeleton of a two-column product grid. */
@Composable
fun ProductGridSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(8.dp).clearAndSetSemantics { },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) { ProductCardSkeleton(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ShimmerBlock(Modifier.fillMaxWidth().height(150.dp), cornerRadius = 16)
        ShimmerBlock(Modifier.fillMaxWidth(0.8f).height(16.dp))
        ShimmerBlock(Modifier.fillMaxWidth(0.4f).height(16.dp))
    }
}

/** Skeleton of a vertical list of cards (orders, cart). */
@Composable
fun ListSkeleton(modifier: Modifier = Modifier, rows: Int = 4) {
    Column(
        modifier = modifier.fillMaxSize().clearAndSetSemantics { },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(rows) { ShimmerBlock(Modifier.fillMaxWidth().height(88.dp), cornerRadius = 16) }
        Spacer(Modifier)
    }
}
