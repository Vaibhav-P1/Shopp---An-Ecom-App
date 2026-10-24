package com.example.shopp.data

import com.example.shopp.domain.Coupon
import com.example.shopp.domain.CouponType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Coupons live at `coupons/{CODE}` (upper-case id), are read-only for the app and managed in the console. */
@Singleton
class CouponRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getCoupon(code: String): Result<Coupon> = firestoreCall {
        val normalized = normalize(code)
        if (normalized.isEmpty()) throw IllegalArgumentException("Enter a coupon code")
        firestore.collection(COLLECTION).document(normalized).get().await().toCoupon()
            ?: throw NoSuchElementException("Invalid coupon code")
    }

    companion object {
        const val COLLECTION = "coupons"

        fun normalize(code: String): String = code.trim().uppercase()
    }
}

/** Maps a coupon document, or null if it does not exist or is malformed. */
internal fun DocumentSnapshot.toCoupon(): Coupon? {
    if (!exists()) return null
    val type = getString("type")?.let { runCatching { CouponType.valueOf(it) }.getOrNull() } ?: return null
    return Coupon(
        code = id,
        type = type,
        value = getLong("value") ?: return null,
        minOrderPaise = getLong("minOrderPaise") ?: 0,
        maxDiscountPaise = getLong("maxDiscountPaise") ?: 0,
        expiresAtMillis = getTimestamp("expiresAt")?.toDate()?.time,
        active = getBoolean("active") ?: true,
        description = getString("description").orEmpty()
    )
}
