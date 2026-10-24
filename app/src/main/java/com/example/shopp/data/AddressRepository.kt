package com.example.shopp.data

import com.example.shopp.model.AddressModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private fun addresses(uid: String): CollectionReference =
        firestore.collection("users").document(uid).collection("addresses")

    private fun requireUid(): String = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")

    /** Live address book, primary address first. Fails the flow if the listener errors. */
    fun observeAddresses(): Flow<List<AddressModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = addresses(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { doc -> doc.toObject(AddressModel::class.java)?.copy(id = doc.id) }
                ?.sortedWith(compareByDescending<AddressModel> { it.primary }.thenBy { it.label.lowercase() })
                .orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Creates the address, or updates it when [AddressModel.id] is set. The first address is always primary,
     * and making one primary un-marks the others in the same batch.
     */
    suspend fun save(address: AddressModel): Result<Unit> = firestoreCall {
        val collection = addresses(requireUid())
        val existing = collection.get().await().documents
        val ref = if (address.id.isBlank()) collection.document() else collection.document(address.id)
        val makePrimary = address.primary || existing.none { it.id != ref.id }

        firestore.runBatch { batch ->
            batch.set(ref, address.toFields(primary = makePrimary))
            if (makePrimary) {
                existing.filter { it.id != ref.id && it.getBoolean("primary") == true }
                    .forEach { batch.update(it.reference, "primary", false) }
            }
        }.await()
        Unit
    }

    suspend fun setPrimary(addressId: String): Result<Unit> = firestoreCall {
        val existing = addresses(requireUid()).get().await().documents
        firestore.runBatch { batch ->
            existing.forEach { batch.update(it.reference, "primary", it.id == addressId) }
        }.await()
        Unit
    }

    /** Deleting the primary address promotes another one, so checkout always has a destination. */
    suspend fun delete(addressId: String): Result<Unit> = firestoreCall {
        val existing = addresses(requireUid()).get().await().documents
        val removed = existing.firstOrNull { it.id == addressId } ?: return@firestoreCall Unit
        val promote = if (removed.getBoolean("primary") == true) existing.firstOrNull { it.id != addressId } else null

        firestore.runBatch { batch ->
            batch.delete(removed.reference)
            promote?.let { batch.update(it.reference, "primary", true) }
        }.await()
        Unit
    }

    private fun AddressModel.toFields(primary: Boolean): Map<String, Any> = mapOf(
        "label" to label.trim(),
        "recipient" to recipient.trim(),
        "phone" to phone.trim(),
        "street" to street.trim(),
        "city" to city.trim(),
        "pincode" to pincode.trim(),
        "primary" to primary
    )
}
