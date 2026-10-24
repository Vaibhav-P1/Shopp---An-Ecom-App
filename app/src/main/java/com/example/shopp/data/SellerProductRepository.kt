package com.example.shopp.data

import com.example.shopp.domain.ProductDraft
import com.example.shopp.model.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** A seller's view of the catalogue: only their own products, including hidden ones. */
@Singleton
class SellerProductRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val products: CollectionReference
        get() = firestore.collection("data").document("stock").collection("products")

    private fun requireUid(): String = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")

    /** Live list of the signed-in seller's products, sorted by title. */
    fun observeMyProducts(): Flow<List<ProductModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = products.whereEqualTo("sellerId", uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { doc -> doc.toObject(ProductModel::class.java)?.copy(id = doc.id) }
                ?.sortedBy { it.title.lowercase() }
                .orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    /** Creates a product, or updates the one with [productId]. The draft must already be validated. */
    suspend fun save(productId: String?, draft: ProductDraft): Result<Unit> = firestoreCall {
        val uid = requireUid()
        val ref = if (productId == null) products.document() else products.document(productId)
        ref.set(
            mapOf(
                "sellerId" to uid,
                "title" to draft.title.trim(),
                "description" to draft.description.trim(),
                "category" to draft.category,
                "price" to draft.price.trim(),
                "actualPrice" to draft.actualPrice.trim(),
                "images" to draft.images,
                "stock" to draft.stock.trim().toLong(),
                "active" to draft.active,
                "otherDetails" to emptyMap<String, String>()
            )
        ).await()
        Unit
    }

    suspend fun setActive(productId: String, active: Boolean): Result<Unit> = firestoreCall {
        products.document(productId).update("active", active).await()
        Unit
    }

    suspend fun delete(productId: String): Result<Unit> = firestoreCall {
        products.document(productId).delete().await()
        Unit
    }
}
