package com.example.shopp

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
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

    // Firestore returns cart quantities as Long, but never trust the raw cast
    private fun parseCart(raw: Any?): Map<String, Long> =
        (raw as? Map<*, *>)
            ?.mapNotNull { (key, value) ->
                val productId = key as? String ?: return@mapNotNull null
                val qty = (value as? Number)?.toLong() ?: return@mapNotNull null
                productId to qty
            }
            ?.toMap()
            .orEmpty()

    fun addItemToCart(productId: String, context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            showToast(context, "Please sign in first")
            return
        }
        val userDoc = Firebase.firestore.collection("users").document(currentUserId)

        // increment is atomic, so rapid taps can't overwrite each other
        userDoc.update("cartItems.$productId", FieldValue.increment(1))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showToast(context, "Item added to the cart")
                } else {
                    showToast(context, "Failed adding item to the cart")
                }
            }
    }

    fun removeFromCart(productId: String, context: Context, removeAll: Boolean = false) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            showToast(context, "Please sign in first")
            return
        }
        val userDoc = Firebase.firestore.collection("users").document(currentUserId)

        Firebase.firestore.runTransaction { transaction ->
            val currentCart = parseCart(transaction.get(userDoc).get("cartItems"))
            val updatedQuantity = (currentCart[productId] ?: 0L) - 1

            if (updatedQuantity <= 0 || removeAll)
                transaction.update(userDoc, "cartItems.$productId", FieldValue.delete())
            else
                transaction.update(userDoc, "cartItems.$productId", updatedQuantity)
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                showToast(context, "Item removed from the cart")
            } else {
                showToast(context, "Failed removing item from the cart")
            }
        }
    }

    fun clearCartAndAddToOrders() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDoc = Firebase.firestore.collection("users").document(currentUserId)

        userDoc.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val currentCart = parseCart(it.result.get("cartItems"))
                if (currentCart.isEmpty()) return@addOnCompleteListener

                val order = OrderModel(
                    id = "ORD_" + UUID.randomUUID().toString().replace("-", "").take(10).uppercase(),
                    userId = currentUserId,
                    date = Timestamp.now(),
                    items = currentCart,
                    status = "ORDERED",
                    address = it.result.getString("address").orEmpty()
                )
                Firebase.firestore
                    .collection("orders")
                    .document(order.id)
                    .set(order)
                    .addOnCompleteListener { orderTask ->
                        if (orderTask.isSuccessful) {
                            userDoc.update("cartItems", FieldValue.delete())
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

            // Gateway accepts amounts in the smallest unit (paise); round, don't truncate
            val calculatedSubunitAmount = Math.round(amount * 100)
            options.put("amount", calculatedSubunitAmount)

            options.put("currency", "INR")

            val currentActivityContext = GlobalNavigation.navController.context as? Activity
                ?: return
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

    private const val PREF_NAME = "favorite_pref"
    private const val KEY_FAVORITES = "favorite_list"

    // 1. Fetch entire stored local wishlist identifier list records
    fun getFavoriteList(context: Context): Set<String> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    // 2. Toggle item collection addition or subtraction operations
    fun addOrRemoveFromFavorite(context: Context, productId: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentFavoritesSet = getFavoriteList(context).toMutableSet()

        if (currentFavoritesSet.contains(productId)) {
            currentFavoritesSet.remove(productId)
            showToast(context, "Item removed from favorite")
        } else {
            currentFavoritesSet.add(productId)
            showToast(context, "Item added to favorite")
        }

        // Commit updated tracking matrices back into local partitions
        sharedPreferences.edit(){
            putStringSet(KEY_FAVORITES, currentFavoritesSet)
        }
    }

    // 3. Validation helper flag to assert current state checks
    fun checkFavorite(context: Context, productId: String): Boolean {
        return getFavoriteList(context).contains(productId)
    }

}
