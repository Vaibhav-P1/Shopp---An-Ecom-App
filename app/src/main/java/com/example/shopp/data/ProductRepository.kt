package com.example.shopp.data

import com.example.shopp.model.ProductModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Opaque marker for "continue after this product" in a paged query. */
class PageCursor internal constructor(internal val snapshot: DocumentSnapshot)

class ProductPage(val items: List<ProductModel>, val nextCursor: PageCursor?) {
    val endReached: Boolean get() = nextCursor == null
}

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val products: CollectionReference
        get() = firestore.collection("data").document("stock").collection("products")

    /**
     * One page of a category, ordered by document id so the cursor is stable. Pass the previous page's
     * [ProductPage.nextCursor] to continue; it is null once the last page has been returned.
     */
    suspend fun getProductsByCategory(
        categoryId: String,
        cursor: PageCursor? = null,
        pageSize: Int = PAGE_SIZE
    ): Result<ProductPage> = firestoreCall {
        var query = products
            .whereEqualTo("category", categoryId)
            .orderBy(FieldPath.documentId())
            .limit(pageSize.toLong())
        if (cursor != null) query = query.startAfter(cursor.snapshot)

        val documents = query.get().await().documents
        ProductPage(
            items = documents.mapNotNull { it.toProduct() },
            // A full page may still be the last one; the next request then returns an empty page
            nextCursor = if (documents.size < pageSize) null else documents.lastOrNull()?.let(::PageCursor)
        )
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

    private companion object {
        const val PAGE_SIZE = 10
    }

    private fun DocumentSnapshot.toProduct(): ProductModel? =
        toObject(ProductModel::class.java)?.let { it.copy(id = it.id.ifBlank { id }) }
}
