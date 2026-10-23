package com.example.shopp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HeaderView(name: String, onSearchClick: () -> Unit, modifier: Modifier = Modifier) {

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column{
            Text(text = "Welcome back")
            Text(text = name,
                style  = TextStyle(
                    fontSize = 18.sp,
                    fontWeight =  FontWeight.Bold
                ))
        }

        IconButton(onClick = onSearchClick) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        }
    }
}
