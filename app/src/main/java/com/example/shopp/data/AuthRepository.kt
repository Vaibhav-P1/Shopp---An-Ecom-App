package com.example.shopp.data

import com.example.shopp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun login(email: String, password: String): Result<Unit> = runCatchingAuth {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signup(email: String, name: String, password: String): Result<Unit> = runCatchingAuth {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Signup failed, please try again")
        firestore.collection("users").document(user.uid)
            .set(UserModel(name = name, email = email, uid = user.uid)).await()
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatchingAuth {
        val user = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
            ?: error("Sign in failed, please try again")
        try {
            createUserDocIfMissing(user)
        } catch (e: Exception) {
            // Don't leave a signed-in session behind when the profile can't be read or created
            auth.signOut()
            throw e
        }
    }

    private suspend fun createUserDocIfMissing(user: FirebaseUser) {
        val doc = firestore.collection("users").document(user.uid)
        if (!doc.get().await().exists()) {
            doc.set(UserModel(name = user.displayName ?: "", email = user.email ?: "", uid = user.uid)).await()
        }
    }

    private suspend fun runCatchingAuth(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
