package com.example.shopp.data

import com.example.shopp.model.ProductModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val products: CollectionReference
        get() = firestore.collection("data").document("stock").collection("products")

    suspend fun getProductsByCategory(categoryId: String): Result<List<ProductModel>> = firestoreCall {
        products.whereEqualTo("category", categoryId).get().await()
            .documents
            .mapNotNull { it.toProduct() }
    }

    suspend fun getProduct(productId: String): Result<ProductModel> = firestoreCall {
        products.document(productId).get().await().toProduct()
            ?: throw NoSuchElementException("This product is no longer available")
    }

    /** Firestore allows at most 30 values per `whereIn`, so ids are fetched in chunks. */
    suspend fun getProductsByIds(ids: Collection<String>): Result<List<ProductModel>> = firestoreCall {
        ids.distinct().chunked(30).flatMap { chunk ->
            products.whereIn(FieldPath.documentId(), chunk).get().await()
                .documents
                .mapNotNull { it.toProduct() }
        }
    }

    private fun DocumentSnapshot.toProduct(): ProductModel? =
        toObject(ProductModel::class.java)?.let { it.copy(id = it.id.ifBlank { id }) }
}
