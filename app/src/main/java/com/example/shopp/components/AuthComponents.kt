package com.example.shopp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.shopp.domain.PasswordStrength
import com.example.shopp.ui.theme.Spacing

/** Round tonal back button used at the top of the auth screens. */
@Composable
fun RoundBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}

/** "or continue with" style divider with a centred label. */
@Composable
fun LabeledDivider(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
    }
}

/** Footer line such as "New to Shopp? Create an account" with a tappable emphasised part. */
@Composable
fun SwitchAuthPrompt(prompt: String, action: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val actionColor = MaterialTheme.colorScheme.primary
    Text(
        text = buildAnnotatedString {
            append("$prompt ")
            withStyle(SpanStyle(color = actionColor, fontWeight = FontWeight.SemiBold)) { append(action) }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = Spacing.sm)
    )
}

/** Four-segment password strength meter with a caption. */
@Composable
fun PasswordStrengthMeter(strength: PasswordStrength, modifier: Modifier = Modifier) {
    val filled = when (strength) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.FAIR -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < strength.bars) filled else MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = strength.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
