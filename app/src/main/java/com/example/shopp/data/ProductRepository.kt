package com.example.shopp.data

import com.example.shopp.model.ProductModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
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

    private fun DocumentSnapshot.toProduct(): ProductModel? =
        toObject(ProductModel::class.java)?.let { it.copy(id = it.id.ifBlank { id }) }
}
