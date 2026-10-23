package com.example.shopp.data

import kotlinx.coroutines.CancellationException

/** Runs [block] and wraps the outcome in a [Result], letting coroutine cancellation through. */
internal suspend inline fun <T> firestoreCall(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
