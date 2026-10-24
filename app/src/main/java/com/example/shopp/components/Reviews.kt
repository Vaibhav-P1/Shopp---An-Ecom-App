package com.example.shopp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shopp.domain.ReviewSummary
import com.example.shopp.model.ReviewModel
import com.example.shopp.ui.formatDate
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.ReviewsUiState

private const val MAX_REVIEW_LENGTH = 500

/** Five stars with [rating] of them filled. Read-only; see [StarPicker] for input. */
@Composable
fun RatingStars(rating: Int, modifier: Modifier = Modifier, starSize: Dp = 16.dp) {
    Row(modifier.semantics { contentDescription = "$rating out of 5 stars" }) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

/** Compact "star 4.3 (12 reviews)" line shown under the product price. */
@Composable
fun RatingSummaryLine(summary: ReviewSummary, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.padding(start = Spacing.xs))
        Text(
            text = if (summary.count == 0) "No reviews yet" else "${summary.averageLabel} (${summary.count} ${if (summary.count == 1) "review" else "reviews"})",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ReviewsSection(
    state: ReviewsUiState,
    isSaving: Boolean,
    onSubmit: (rating: Int, text: String) -> Unit,
    onDeleteMine: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        ReviewDialog(
            initial = state.myReview,
            onDismiss = { showDialog = false },
            onSubmit = { rating, text ->
                showDialog = false
                onSubmit(rating, text)
            }
        )
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text("Ratings & reviews", style = MaterialTheme.typography.titleMedium)
        RatingSummaryLine(state.summary)

        when {
            state.reviewOrderId != null -> TextButton(onClick = { showDialog = true }, enabled = !isSaving) {
                Text(if (state.myReview == null) "Write a review" else "Edit your review")
            }
            state.myReview == null -> Text(
                text = "Only customers with a delivered order can review this product.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        state.reviews.forEach { review ->
            ReviewItem(review = review, isMine = review.userId == state.myReview?.userId, onDelete = onDeleteMine)
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewModel, isMine: Boolean, onDelete: () -> Unit) {
    ShoppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isMine) "${review.userName} (you)" else review.userName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    review.updatedAt?.let {
                        Text(it.formatDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isMine) TextButton(onClick = onDelete) { Text("Delete") }
            }
            RatingStars(review.rating.toInt())
            if (review.text.isNotBlank()) Text(review.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewDialog(
    initial: ReviewModel?,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, text: String) -> Unit
) {
    var rating by remember { mutableIntStateOf(initial?.rating?.toInt() ?: 0) }
    var text by remember { mutableStateOf(initial?.text.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Write a review" else "Edit your review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                StarPicker(rating = rating, onRatingChange = { rating = it })
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(MAX_REVIEW_LENGTH) },
                    label = { Text("Share your experience (optional)") },
                    minLines = 3,
                    supportingText = { Text("${text.length}/$MAX_REVIEW_LENGTH") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(rating, text) }, enabled = rating in 1..5) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StarPicker(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        (1..5).forEach { value ->
            IconButton(onClick = { onRatingChange(value) }) {
                Icon(
                    imageVector = if (value <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$value star${if (value == 1) "" else "s"}",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
