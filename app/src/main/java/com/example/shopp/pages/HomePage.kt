package com.example.shopp.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.components.BannerView
import com.example.shopp.components.CategoriesView
import com.example.shopp.components.HeaderView

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
    ){
        HeaderView(modifier)

        Spacer(modifier = Modifier.height(10.dp))

        BannerView(modifier = Modifier.height(150.dp))

        // Categories:
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        CategoriesView()
    }
}