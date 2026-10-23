package com.example.shopp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /** First word of the signed-in user's name, or empty when signed out or unset. */
    suspend fun getFirstName(): Result<String> = firestoreCall {
        val uid = auth.currentUser?.uid ?: return@firestoreCall ""
        firestore.collection("users").document(uid).get().await()
            .getString("name").orEmpty()
            .trim().substringBefore(' ')
    }
}
