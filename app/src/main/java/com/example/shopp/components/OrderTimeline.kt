package com.example.shopp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shopp.model.OrderStatus

/** Horizontal progress of an order through [OrderStatus.timeline]; steps up to the current one are filled. */
@Composable
fun OrderTimeline(status: String, modifier: Modifier = Modifier) {
    val currentIndex = OrderStatus.timeline.indexOf(status)
    val steps = OrderStatus.timeline
    val done = MaterialTheme.colorScheme.primary
    val pending = MaterialTheme.colorScheme.surfaceContainerHighest

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Order status: ${OrderStatus.label(status)}" },
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, step ->
            val reached = index <= currentIndex
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Connector(visible = index > 0, color = if (index <= currentIndex) done else pending, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (reached) done else pending),
                        contentAlignment = Alignment.Center
                    ) {
                        if (reached) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Connector(
                        visible = index < steps.lastIndex,
                        color = if (index < currentIndex) done else pending,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = OrderStatus.label(step),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Connector(visible: Boolean, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(modifier.height(3.dp).background(if (visible) color else androidx.compose.ui.graphics.Color.Transparent))
}

/**
 * Vertical version of the progress timeline for the order details page: a dot per step joined by a
 * line, the current step highlighted in coral, with optional per-step [captions].
 */
@Composable
fun VerticalOrderTimeline(
    status: String,
    modifier: Modifier = Modifier,
    captions: Map<String, String> = emptyMap()
) {
    val steps = OrderStatus.timeline
    val currentIndex = steps.indexOf(status)
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.semantics { contentDescription = "Order status: ${OrderStatus.label(status)}" }
    ) {
        steps.forEachIndexed { index, step ->
            val done = index < currentIndex || (index == currentIndex && status == OrderStatus.DELIVERED)
            val current = index == currentIndex && !done
            val title = if (step == OrderStatus.ORDERED) "Order placed" else OrderStatus.label(step)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                        if (current) {
                            Box(Modifier.requiredSize(36.dp).clip(CircleShape).background(colors.secondaryContainer))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        done -> colors.primary
                                        current -> colors.secondary
                                        else -> colors.surfaceContainerHighest
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (done) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            Modifier
                                .width(3.dp)
                                .height(26.dp)
                                .background(if (index < currentIndex) colors.primary else colors.surfaceVariant)
                        )
                    }
                }
                Column(Modifier.padding(top = 2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (current) FontWeight.Bold else if (done) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = when {
                            current -> colors.secondary
                            done -> colors.onSurface
                            else -> colors.outline
                        }
                    )
                    captions[step]?.let {
                        Text(it, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
