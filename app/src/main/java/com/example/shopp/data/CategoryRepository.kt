package com.example.shopp.data

import com.example.shopp.model.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getCategories(): Result<List<CategoryModel>> = firestoreCall {
        firestore.collection("data").document("stock").collection("categories")
            .get().await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(CategoryModel::class.java)?.let { it.copy(id = it.id.ifBlank { doc.id }) }
            }
    }
}
