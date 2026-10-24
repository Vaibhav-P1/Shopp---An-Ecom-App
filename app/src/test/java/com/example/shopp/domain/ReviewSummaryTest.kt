package com.example.shopp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewSummaryTest {

    @Test
    fun `no reviews gives an empty summary`() {
        assertEquals(ReviewSummary.Empty, ReviewSummary.of(emptyList()))
        assertEquals("-", ReviewSummary.Empty.averageLabel)
    }

    @Test
    fun `averages ratings and rounds the label to one decimal`() {
        val summary = ReviewSummary.of(listOf(5, 4, 4))
        assertEquals(3, summary.count)
        assertEquals("4.3", summary.averageLabel)
    }

    @Test
    fun `ignores ratings outside 1 to 5`() {
        val summary = ReviewSummary.of(listOf(5, 0, 9, 3))
        assertEquals(2, summary.count)
        assertEquals("4.0", summary.averageLabel)
    }
}
