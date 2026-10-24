package com.example.shopp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shopp.ui.theme.Spacing

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StateIcon(Icons.Outlined.CloudOff, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(text = "Something went wrong", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        PrimaryButton(text = "Retry", onClick = onRetry, modifier = Modifier.padding(horizontal = Spacing.xxl))
    }
}

/** Empty state with a tinted icon, a message, and an optional call to action. */
@Composable
fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StateIcon(icon, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(text = message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            PrimaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.padding(horizontal = Spacing.xxl))
        }
    }
}

@Composable
private fun StateIcon(icon: ImageVector, container: Color, content: Color) {
    Box(
        modifier = Modifier.size(96.dp).clip(CircleShape).background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(44.dp), tint = content)
    }
}
