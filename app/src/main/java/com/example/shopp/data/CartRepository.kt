package com.example.shopp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private fun userDoc() = auth.currentUser?.uid?.let { firestore.collection("users").document(it) }

    /** Live cart as productId -> quantity. Fails the flow if the listener errors. */
    fun observeCart(): Flow<Map<String, Long>> = callbackFlow {
        val doc = userDoc()
        if (doc == null) {
            trySend(emptyMap())
            awaitClose { }
            return@callbackFlow
        }
        val registration = doc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(parseCart(snapshot?.get("cartItems")))
        }
        awaitClose { registration.remove() }
    }

    /** Atomic increment, so rapid taps can't overwrite each other. Creates the user doc if missing. */
    suspend fun addItem(productId: String): Result<Unit> = firestoreCall {
        val doc = userDoc() ?: throw IllegalStateException("Please sign in first")
        doc.set(mapOf("cartItems" to mapOf(productId to FieldValue.increment(1))), SetOptions.merge()).await()
    }

    suspend fun decrementItem(productId: String): Result<Unit> = firestoreCall {
        val doc = userDoc() ?: throw IllegalStateException("Please sign in first")
        firestore.runTransaction { transaction ->
            val quantity = parseCart(transaction.get(doc).get("cartItems"))[productId] ?: 0L
            if (quantity <= 1) {
                transaction.update(doc, "cartItems.$productId", FieldValue.delete())
            } else {
                transaction.update(doc, "cartItems.$productId", quantity - 1)
            }
        }.await()
        Unit
    }

    suspend fun removeItem(productId: String): Result<Unit> = firestoreCall {
        val doc = userDoc() ?: throw IllegalStateException("Please sign in first")
        doc.update("cartItems.$productId", FieldValue.delete()).await()
        Unit
    }

    // Firestore returns quantities as Long, but never trust the raw cast
    private fun parseCart(raw: Any?): Map<String, Long> =
        (raw as? Map<*, *>)
            ?.mapNotNull { (key, value) ->
                val productId = key as? String ?: return@mapNotNull null
                val quantity = (value as? Number)?.toLong() ?: return@mapNotNull null
                productId to quantity
            }
            ?.toMap()
            .orEmpty()
}
