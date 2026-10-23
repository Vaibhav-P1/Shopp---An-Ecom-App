package com.example.shopp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shopp.model.CategoryModel

@Composable
fun CategoriesView(
    categories: List<CategoryModel>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(categories) { item ->
            CategoryItem(category = item, onClick = { onCategoryClick(item.id) })
        }
    }
}

@Composable
fun CategoryItem(category: CategoryModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ){
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                textAlign = TextAlign.Center
            )
        }
    }
}