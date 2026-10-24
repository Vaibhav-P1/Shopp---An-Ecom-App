package com.example.shopp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shopp.ui.theme.Spacing

private val ButtonHeight = 56.dp

/** Full-width filled button. While [isLoading] it shows a spinner and ignores taps. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Full-width outlined button. [leadingContent] is shown before the label, e.g. an icon. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingContent: (@Composable RowScope.() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
    ) {
        if (leadingContent != null) {
            leadingContent()
            Spacer(Modifier.size(Spacing.sm))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Full-width filled button with a label on the left and a value (usually a total) on the right. */
@Composable
fun SpaceBetweenButton(
    start: String,
    end: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.large,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = start, style = MaterialTheme.typography.titleMedium)
                Text(text = end, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
