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

/** Favorite product ids, stored as the `favorites` array on the user's profile doc. */
@Singleton
class FavoritesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private fun userDoc() = auth.currentUser?.uid?.let { firestore.collection("users").document(it) }

    /** Live set of favorite product ids. Fails the flow if the listener errors. */
    fun observeFavorites(): Flow<Set<String>> = callbackFlow {
        val doc = userDoc()
        if (doc == null) {
            trySend(emptySet())
            awaitClose { }
            return@callbackFlow
        }
        val registration = doc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ids = (snapshot?.get("favorites") as? List<*>).orEmpty().filterIsInstance<String>()
            trySend(ids.toSet())
        }
        awaitClose { registration.remove() }
    }

    /** Atomic add/remove, so two quick taps or two devices can't overwrite each other. */
    suspend fun setFavorite(productId: String, favorite: Boolean): Result<Unit> = firestoreCall {
        val doc = userDoc() ?: throw IllegalStateException("Please sign in first")
        val update = if (favorite) FieldValue.arrayUnion(productId) else FieldValue.arrayRemove(productId)
        doc.set(mapOf("favorites" to update), SetOptions.merge()).await()
        Unit
    }
}
