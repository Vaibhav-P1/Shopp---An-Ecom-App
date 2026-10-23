package com.example.shopp.data

import com.example.shopp.model.OfferModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfferRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getOffers(): Result<List<OfferModel>> = firestoreCall {
        firestore.collection("offers").get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(OfferModel::class.java)?.copy(id = doc.id) }
            .sortedBy { it.priority }
    }
}
