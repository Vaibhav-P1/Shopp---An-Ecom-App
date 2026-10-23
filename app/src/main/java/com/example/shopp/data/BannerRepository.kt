package com.example.shopp.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getBannerUrls(): Result<List<String>> = firestoreCall {
        val document = firestore.collection("data").document("banners").get().await()
        (document.get("urls") as? List<*>)?.filterIsInstance<String>().orEmpty()
    }
}
