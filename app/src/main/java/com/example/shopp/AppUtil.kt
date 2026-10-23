package com.example.shopp

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
