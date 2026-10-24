package com.example.shopp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderStatusTest {

    @Test
    fun `orders advance one step at a time`() {
        assertEquals(OrderStatus.PACKED, OrderStatus.next(OrderStatus.ORDERED))
        assertEquals(OrderStatus.SHIPPED, OrderStatus.next(OrderStatus.PACKED))
        assertEquals(OrderStatus.DELIVERED, OrderStatus.next(OrderStatus.SHIPPED))
    }

    @Test
    fun `delivered, cancelled and unknown orders cannot advance`() {
        assertNull(OrderStatus.next(OrderStatus.DELIVERED))
        assertNull(OrderStatus.next(OrderStatus.CANCELLED))
        assertNull(OrderStatus.next("SOMETHING_ELSE"))
    }

    @Test
    fun `seller action label names the next step`() {
        assertEquals("Mark as packed", OrderStatus.actionLabel(OrderStatus.ORDERED))
        assertEquals("Mark as delivered", OrderStatus.actionLabel(OrderStatus.SHIPPED))
        assertNull(OrderStatus.actionLabel(OrderStatus.DELIVERED))
    }
}
