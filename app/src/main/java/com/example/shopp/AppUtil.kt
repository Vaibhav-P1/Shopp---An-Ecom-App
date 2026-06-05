package com.example.shopp

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.example.shopp.model.OrderModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun addItemToCart(productId: String, context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        val userDoc =
            Firebase.firestore            //Taking document reference because we want to update it
                .collection("users")
                .document(currentUserId)

        userDoc.get().addOnCompleteListener {
            if (it.isSuccessful) {
                // Fetch the existing map of cart items or initialize an empty one
                val currentCart = it.result.get("cartItems") as? Map<String, Long> ?: emptyMap()

                // Read current product quantity or fallback to 0
                val currentQuantity = currentCart[productId] ?: 0L
                val updatedQuantity = currentQuantity + 1

                // Create map variable targeting structural nested values updates
                val updatedCart = mapOf(
                    "cartItems.$productId" to updatedQuantity
                )

                // Update the document in Firestore
                userDoc.update(updatedCart)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showToast(context, "Item added to the cart")
                        } else {
                            showToast(context, "Failed adding item to the cart")
                        }
                    }
            }

        }
    }

    fun removeFromCart(productId: String, context: Context,removeAll : Boolean = false) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        val userDoc =
            Firebase.firestore            //Taking document reference because we want to update it
                .collection("users")
                .document(currentUserId)

        userDoc.get().addOnCompleteListener {
            if (it.isSuccessful) {
                // Fetch the existing map of cart items or initialize an empty one
                val currentCart = it.result.get("cartItems") as? Map<String, Long> ?: emptyMap()

                // Read current product quantity or fallback to 0
                val currentQuantity = currentCart[productId] ?: 0L
                val updatedQuantity = currentQuantity - 1


                // Create map variable targeting structural nested values updates
                val updatedCart =
                    if (updatedQuantity <=0 || removeAll)
                        mapOf("cartItems.$productId" to FieldValue.delete())
                    else
                        mapOf("cartItems.$productId" to updatedQuantity)

                // Update the document in Firestore
                userDoc.update(updatedCart)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showToast(context, "Item removed from the cart")
                        } else {
                            showToast(context, "Failed removing item from the cart")
                        }
                    }
            }

        }

    }

    fun clearCartAndAddToOrders() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        val userDoc =
            Firebase.firestore            //Taking document reference because we want to update it
                .collection("users")
                .document(currentUserId)

        userDoc.get().addOnCompleteListener {
            if (it.isSuccessful) {
                // Fetch the existing map of cart items or initialize an empty one
                val currentCart = it.result.get("cartItems") as? Map<String, Long> ?: emptyMap()

                val order = OrderModel(
                    id ="ORD_"+ UUID.randomUUID().toString().replace("-","").take(10).uppercase(),
                    userId = currentUserId,
                    date = Timestamp.now(),
                    items = currentCart,
                    status = "ORDERED",
                    address = it.result.get("address") as String
                )
                Firebase.firestore
                    .collection("orders")
                    .document(order.id)
                    .set(order)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            userDoc.update("cartItems",FieldValue.delete())
                        }
                    }


            }
        }
    }

    fun getDiscountPercentage(): Float {
        return 10.0f // Represents a 10% discount
    }

    fun getTaxPercentage(): Float {
        return 13.0f // Represents a 13% tax rule
    }

    fun razorpayApiKey() : String{
        return "rzp_test_SuqQDOg4Ffsnbk"
    }

    fun startPayment(amount: Float) {
        val checkout = Checkout()
        checkout.setKeyID(razorpayApiKey())

        try {
            val options = JSONObject()
            options.put("name", "Shopp")
            options.put("description", "E-commerce Order Payment Transaction")

            // Critical transformation: Gateway accepts amounts exclusively in base subunits (cents/paise)
            val calculatedSubunitAmount = (amount * 100).toInt()
            options.put("amount", calculatedSubunitAmount)

            options.put("currency", "INR") // Swap to "INR" for alternative banking portals

            // Resolve context bounds tracking back safely into target instances
            val currentActivityContext = GlobalNavigation.navController.context as Activity
            checkout.open(currentActivityContext, options)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun formatDate(timestamp: Timestamp): String {
        // Formats down into human readable values (e.g., "27 May 2025 04:30 PM")
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
        return simpleDateFormat.format(timestamp.toDate().time)
    }

}