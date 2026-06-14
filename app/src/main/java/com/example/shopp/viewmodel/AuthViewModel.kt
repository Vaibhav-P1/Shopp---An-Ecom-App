package com.example.shopp.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import com.example.shopp.model.UserModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {

    private val auth = Firebase.auth

    private val firestore = Firebase.firestore

    fun login(email: String, password: String, onResult: (Boolean,String?)-> Unit){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    onResult(true, null)
                }else{
                    onResult(false, it.exception?.localizedMessage)
                }
            }
    }

    fun signup(email: String, name: String, password: String, onResult: (Boolean,String?)-> Unit){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    var userId = it.result?.user?.uid

                    val userModel = UserModel(name, email, userId!!)
                    firestore.collection("users").document(userId).set(userModel)
                        .addOnCompleteListener { dbTask ->
                            if(dbTask.isSuccessful){
                                onResult(true, null)
                            }else{
                                onResult(false,"Something went wrong")
                            }
                        }
                }else{
                    onResult(false, it.exception?.localizedMessage)
                }
            }


    }

    fun googleSignIn(
        context: Context,
        scope: CoroutineScope,
        webClientId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // show all accounts, not just previously signed-in
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            val userId = user?.uid ?: return@addOnCompleteListener

                            // Check if user doc exists; if not, create it (new Google user)
                            firestore.collection("users").document(userId).get()
                                .addOnCompleteListener { docTask ->
                                    if (docTask.isSuccessful && !docTask.result.exists()) {
                                        val userModel = UserModel(
                                            name = user.displayName ?: "",
                                            email = user.email ?: "",
                                            uid = userId
                                        )
                                        firestore.collection("users").document(userId)
                                            .set(userModel)
                                    }
                                    onResult(true, null)
                                }
                        } else {
                            onResult(false, task.exception?.localizedMessage)
                        }
                    }

            } catch (e: GetCredentialException) {
                onResult(false, e.localizedMessage)
            }
        }
    }
}