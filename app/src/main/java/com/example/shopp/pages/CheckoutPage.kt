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
import androidx.compose.material3.MaterialTheme
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
        // Start from zero every time so repeated calls never double count
        var runningSubtotal = 0f
        productList.forEach { product ->
            val price = product.actualPrice.toFloatOrNull() ?: return@forEach
            val qty = userModel.value.cartItems[product.id] ?: 0L
            runningSubtotal += price * qty
        }
        subTotal.value = runningSubtotal
        discount.value = runningSubtotal * (AppUtil.getDiscountPercentage() / 100f)
        tax.value = runningSubtotal * (AppUtil.getTaxPercentage() / 100f)

        total.value = runningSubtotal - discount.value + tax.value
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        Firebase.firestore
            .collection("users")
            .document(userId)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val result = it.result.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result

                        // whereIn rejects an empty list and allows at most 30 values
                        val chunks = result.cartItems.keys.toList().chunked(30)
                        var pending = chunks.size
                        val loaded = mutableListOf<ProductModel>()
                        chunks.forEach { ids ->
                            Firebase.firestore
                                .collection("data")
                                .document("stock")
                                .collection("products")
                                .whereIn("id", ids)
                                .get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        loaded.addAll(task.result.toObjects(ProductModel::class.java))
                                    }
                                    pending--
                                    if (pending == 0) {
                                        productList.clear()
                                        productList.addAll(loaded)
                                        calculateAssign()
                                    }
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
        RowCheckoutItems(title = "Subtotal", value = "₹" + "%.2f".format(subTotal.value))
        Spacer(modifier = Modifier.height(8.dp))
        RowCheckoutItems(title = "Discount", value = "-₹" + "%.2f".format(discount.value))
        Spacer(modifier = Modifier.height(8.dp))
        RowCheckoutItems(title = "Tax", value = "+₹" + "%.2f".format(tax.value))
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
            text = "₹" + "%.2f".format(total.value),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (userModel.value.address.isBlank()) {
            Text(
                text = "Add a delivery address in your profile to place an order",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                    AppUtil.startPayment(total.value)
            },
            enabled = total.value > 0f && userModel.value.address.isNotBlank(),
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