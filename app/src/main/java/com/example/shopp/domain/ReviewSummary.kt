package com.example.shopp.domain

import java.util.Locale

/** Average rating and count of a product's reviews, computed on the client from the review documents. */
data class ReviewSummary(val average: Double, val count: Int) {

    /** Average with one decimal, e.g. "4.3"; "-" when there are no reviews. */
    val averageLabel: String get() = if (count == 0) "-" else String.format(Locale.US, "%.1f", average)

    companion object {
        val Empty = ReviewSummary(0.0, 0)

        fun of(ratings: List<Long>): ReviewSummary {
            val valid = ratings.filter { it in 1..5 }
            return if (valid.isEmpty()) Empty else ReviewSummary(valid.average(), valid.size)
        }
    }
}
