package com.example.shopp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Rounded-rectangle choice chip: coral tint with a check when selected, outlined otherwise. */
@Composable
fun ShoppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(36.dp),
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.secondaryContainer,
            selectedLabelColor = colors.onSecondaryContainer,
            selectedLeadingIconColor = colors.onSecondaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outline,
            selectedBorderColor = colors.secondaryContainer
        )
    )
}

/** Dismissible coral chip that shows an active filter. */
@Composable
fun ActiveFilterChip(label: String, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.secondaryContainer)
            .clickable(onClickLabel = "Remove filter $label", role = Role.Button, onClick = onRemove)
            .padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = colors.onSecondaryContainer
        )
        Icon(
            Icons.Default.Close,
            contentDescription = null,
            tint = colors.onSecondaryContainer,
            modifier = Modifier.size(14.dp)
        )
    }
}
