package com.example.shopp

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
}