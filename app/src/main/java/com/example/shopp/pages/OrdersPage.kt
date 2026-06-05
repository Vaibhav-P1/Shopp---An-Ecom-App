package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.components.OrderView
import com.example.shopp.model.OrderModel
import com.example.shopp.model.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun OrdersPage(modifier: Modifier = Modifier){
    val orderList = remember { mutableStateOf<List<OrderModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        Firebase.firestore
            .collection("orders")
            .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid!!)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val resultList = it.result.documents.mapNotNull { document ->
                        document.toObject(OrderModel::class.java)
                    }
                    orderList.value = resultList
//                        .plus(resultList).plus(resultList)  it allows to show more data
                }
            }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Your Orders",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))



        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orderList.value) { item ->
                OrderView(orderItem = item)
            }
        }
    }
}