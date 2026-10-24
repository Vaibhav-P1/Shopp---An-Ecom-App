package com.example.shopp.domain

import com.example.shopp.model.OrderLineModel
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SellerOrdersTest {

    private fun line(sellerId: String, price: Long, qty: Long) =
        OrderLineModel(productId = "p$price", unitPricePaise = price, quantity = qty, sellerId = sellerId)

    private fun order(status: String, vararg lines: OrderLineModel) =
        OrderModel(id = "o", status = status, lines = lines.toList())

    @Test
    fun `only the sellers own lines count towards their revenue`() {
        val order = order(OrderStatus.DELIVERED, line("a", 10_000, 2), line("b", 5_000, 1))
        assertEquals(20_000, order.revenueOf("a"))
        assertEquals(5_000, order.revenueOf("b"))
        assertEquals(0, order.revenueOf("c"))
        assertEquals(1, order.linesOf("b").size)
    }

    @Test
    fun `stats count by status and earn revenue only when delivered`() {
        val orders = listOf(
            order(OrderStatus.ORDERED, line("a", 1_000, 1)),
            order(OrderStatus.PACKED, line("a", 2_000, 1)),
            order(OrderStatus.SHIPPED, line("a", 3_000, 1)),
            order(OrderStatus.DELIVERED, line("a", 4_000, 2), line("b", 9_000, 1)),
            order(OrderStatus.CANCELLED, line("a", 50_000, 1))
        )
        val stats = SellerStatsCalculator.calculate(orders, "a")
        assertEquals(1, stats.newOrders)
        assertEquals(2, stats.inProgress)
        assertEquals(1, stats.delivered)
        assertEquals(8_000, stats.revenuePaise)
    }
}
