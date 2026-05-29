package com.example.shopp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.AppUtil
import com.example.shopp.GlobalNavigation
import com.example.shopp.model.ProductModel
import com.example.shopp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun CheckoutPage(modifier: Modifier = Modifier) {
    var userModel = remember { mutableStateOf(UserModel()) }
    val productList = remember { mutableStateListOf<ProductModel>() }

    var subTotal = remember { mutableStateOf(0f) }
    var discount = remember { mutableStateOf(0f) }
    var tax = remember { mutableStateOf(0f) }
    var total = remember { mutableStateOf(0f) }

    fun calculateAssign() {
        var runningSubtotal = 0f
        productList.forEach { product ->
            if (product.actualPrice.isNotEmpty()) {
                val qty = userModel.value.cartItems[product.id] ?: 0L
                subTotal.value += product.actualPrice.toFloat() * qty
            }
        }
        discount.value = subTotal.value * (AppUtil.getDiscountPercentage() / 100f)
        tax.value = subTotal.value * (AppUtil.getTaxPercentage() / 100f)

        total.value = subTotal.value - discount.value + tax.value
        // Formats down precisely to two decimal value targets
//        total = String.format("%.2f", total).toFloat()
    }

    LaunchedEffect(Unit) {
        Firebase.firestore
            .collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val result = it.result.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result

                        Firebase.firestore
                            .collection("data")
                            .document("stock")
                            .collection("products")
                            .whereIn("id", userModel.value.cartItems.keys.toList())
                            .get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val resultProducts =
                                        task.result.toObjects(ProductModel::class.java)
                                    productList.addAll(resultProducts)
                                    calculateAssign()
                                }
                            }
                    }
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
//            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Checkout",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text= "Deliver to :", fontWeight = FontWeight.SemiBold)
        Text(text= userModel.value.name)
        Text(text= userModel.value.address)
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Billing Rows Breakdown
        RowCheckoutItems(title = "Subtotal", value = "₹"+subTotal.value.toString())
        Spacer(modifier = Modifier.height(8.dp))
        RowCheckoutItems(title = "Discount", value = "-₹"+discount.value.toString())
        Spacer(modifier = Modifier.height(8.dp))
        RowCheckoutItems(title = "Tax", value = "+₹"+tax.value.toString())
//
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
//
        // Final Aggregate Row Outputs
        Text(
            text = "Total To Pay",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Text(
            text = "₹"+total.value.toString(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                    AppUtil.startPayment(total.value)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Pay Now", fontSize = 16.sp)
        }
    }

}

@Composable
fun RowCheckoutItems(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 18.sp)
    }
}