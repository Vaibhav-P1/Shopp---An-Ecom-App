package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.AppUtil
import com.example.shopp.components.ProductItemView
import com.example.shopp.model.ProductModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun FavoritePage(modifier: Modifier = Modifier) {
    val productList = remember {
        mutableStateOf<List<ProductModel>>(emptyList())
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val favoriteList = AppUtil.getFavoriteList(context)
        if(favoriteList.isEmpty()) {
            productList.value = emptyList()
        }else {
            Firebase.firestore
                .collection("data")
                .document("stock")
                .collection("products")
                .whereIn("id", favoriteList.toList())
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val resultList = it.result.documents.mapNotNull { document ->
                            document.toObject(ProductModel::class.java)
                        }
                        productList.value = resultList
//                        .plus(resultList).plus(resultList)  it allows to show more data
                    }
                }
        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Favorites",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (productList.value.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(productList.value.chunked(2)) { rowItems ->
                    Row {
                        rowItems.forEach {
                            ProductItemView(product = it, modifier = Modifier.weight(1f))
                        }
                        // If a row has only 1 product, append an empty spacer to maintain layout sizing
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }else{
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = "No items in favorites",
                    fontSize = 30.sp)
            }
        }

    }
}