package com.example.shopp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.model.CategoryModel

@Composable
fun CategoriesView(
    categories: List<CategoryModel>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tints = listOf(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.primaryContainer
    )
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(categories) { index, item ->
            CategoryItem(
                category = item,
                tint = tints[index % tints.size],
                onClick = { onCategoryClick(item.id) }
            )
        }
    }
}

/** Rounded pastel tile holding the category image, with its name underneath. */
@Composable
fun CategoryItem(category: CategoryModel, tint: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Open ${category.name}", role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(22.dp)).background(tint),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = null, // the name is shown as text below
                modifier = Modifier.size(38.dp)
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
