package com.example.shopp.data

import com.example.shopp.model.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {

    private fun reviews(productId: String): CollectionReference =
        firestore.collection("data").document("stock").collection("products").document(productId).collection("reviews")

    /** Live reviews of a product, newest first. */
    fun observeReviews(productId: String): Flow<List<ReviewModel>> = callbackFlow {
        val registration = reviews(productId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { doc -> doc.toObject(ReviewModel::class.java)?.copy(userId = doc.id) }
                // A just-written review has no server timestamp yet; treat it as the newest
                ?.sortedByDescending { it.updatedAt?.seconds ?: Long.MAX_VALUE }
                .orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Creates or replaces the signed-in user's review. [orderId] is a delivered order containing the product;
     * Firestore rules check it, so only real buyers can review.
     */
    suspend fun submit(productId: String, orderId: String, rating: Int, text: String): Result<Unit> = firestoreCall {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")
        val name = userRepository.getProfile().getOrNull()?.name?.trim().orEmpty().ifBlank { "Customer" }
        reviews(productId).document(uid).set(
            mapOf(
                "userId" to uid,
                "userName" to name,
                "orderId" to orderId,
                "rating" to rating.toLong(),
                "text" to text.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }

    suspend fun delete(productId: String): Result<Unit> = firestoreCall {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")
        reviews(productId).document(uid).delete().await()
        Unit
    }
}
