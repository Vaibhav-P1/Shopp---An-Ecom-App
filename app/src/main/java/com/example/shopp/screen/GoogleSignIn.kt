package com.example.shopp.screen

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.shopp.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Shows the Google account picker, then hands the ID token to the ViewModel. */
fun AuthViewModel.googleSignIn(context: Context, scope: CoroutineScope, webClientId: String) {
    onGoogleStarted()
    scope.launch {
        requestGoogleIdToken(context, webClientId)
            .onSuccess { signInWithGoogle(it) }
            .onFailure { onGoogleFailure(it.localizedMessage) }
    }
}

private suspend fun requestGoogleIdToken(context: Context, webClientId: String): Result<String> {
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
        )
        .build()
    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
