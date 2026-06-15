package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.GlobalNavigation
import com.example.shopp.components.CartItemView
import com.example.shopp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun CartPage(modifier: Modifier = Modifier) {

    var userModel = remember { mutableStateOf(UserModel()) }

    DisposableEffect(Unit)   {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        var listener = Firebase.firestore
            .collection("users")
            .document(currentUserId)
            .addSnapshotListener {it,_ ->      //to see changes frequently
                if (it!=null) {
                    val result = it.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Cart",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        if(userModel.value.cartItems.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                items(userModel.value.cartItems.toList(), key = { it.first }) { (productId, qty) ->
                    CartItemView(productId = productId, qty = qty)
                }
            }
            Button(
                onClick = {
                    GlobalNavigation.navController.navigate("checkout")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Checkout", fontSize = 16.sp)
            }
        }else{
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                 horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = "No items in cart",
                    fontSize = 30.sp)
            }
        }

    }
}