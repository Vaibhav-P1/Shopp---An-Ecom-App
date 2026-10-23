package com.example.shopp.data

import com.example.shopp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /** The signed-in user's profile document, or an empty profile when signed out or not created yet. */
    suspend fun getProfile(): Result<UserModel> = firestoreCall {
        val uid = auth.currentUser?.uid ?: return@firestoreCall UserModel()
        firestore.collection("users").document(uid).get().await()
            .toObject(UserModel::class.java) ?: UserModel()
    }

    /** First word of the signed-in user's name, or empty when signed out or unset. */
    suspend fun getFirstName(): Result<String> = firestoreCall {
        val uid = auth.currentUser?.uid ?: return@firestoreCall ""
        firestore.collection("users").document(uid).get().await()
            .getString("name").orEmpty()
            .trim().substringBefore(' ')
    }

    /** Saves the delivery address. Merges, so it also works for an account whose profile doc is missing. */
    suspend fun updateAddress(address: String): Result<Unit> = firestoreCall {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")
        firestore.collection("users").document(uid)
            .set(mapOf("address" to address.trim()), SetOptions.merge()).await()
        Unit
    }
}
