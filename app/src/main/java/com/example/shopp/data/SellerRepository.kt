package com.example.shopp.data

import com.example.shopp.model.SellerModel
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
class SellerRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /** The signed-in user's seller profile, live; emits null while they have not signed up as a seller. */
    fun observeSeller(): Flow<SellerModel?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = firestore.collection("sellers").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(SellerModel::class.java))
        }
        awaitClose { registration.remove() }
    }

    /**
     * Turns the signed-in user into a seller: creates `sellers/{uid}` and adds the "seller" role to their
     * profile in one batch. Firestore rules only accept the role change when the seller document is written
     * with it.
     */
    suspend fun becomeSeller(shopName: String, phone: String): Result<Unit> = firestoreCall {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")
        firestore.runBatch { batch ->
            batch.set(
                firestore.collection("sellers").document(uid),
                mapOf(
                    "shopName" to shopName.trim(),
                    "phone" to phone.trim(),
                    "status" to "approved",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                firestore.collection("users").document(uid),
                mapOf("roles" to FieldValue.arrayUnion("customer", "seller")),
                SetOptions.merge()
            )
        }.await()
        Unit
    }
}
