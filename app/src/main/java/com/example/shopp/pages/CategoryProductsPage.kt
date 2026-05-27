package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shopp.components.CategoryItem
import com.example.shopp.components.ProductItemView
import com.example.shopp.model.CategoryModel
import com.example.shopp.model.ProductModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun CategoryProductsPage(modifier: Modifier = Modifier,categoryId:String) {
    val productList = remember { mutableStateOf<List<ProductModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        Firebase.firestore
            .collection("data")
            .document("stock")
            .collection("products")
            .whereEqualTo("category",categoryId)
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
}