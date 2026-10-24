package com.example.shopp.domain

import com.example.shopp.model.OrderLineModel
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus

/** The lines of an order that belong to [sellerId]; an order can contain several sellers' products. */
fun OrderModel.linesOf(sellerId: String): List<OrderLineModel> = lines.filter { it.sellerId == sellerId }

/** What [sellerId] earns from this order in paise, before any order-level coupon, tax or shipping. */
fun OrderModel.revenueOf(sellerId: String): Long = linesOf(sellerId).sumOf { it.unitPricePaise * it.quantity }

/** Headline numbers of the seller dashboard, derived from the seller's orders. */
data class SellerStats(
    val newOrders: Int,
    val inProgress: Int,
    val delivered: Int,
    val revenuePaise: Long
)

object SellerStatsCalculator {

    /** Cancelled orders never count; revenue is only earned once an order is delivered. */
    fun calculate(orders: List<OrderModel>, sellerId: String): SellerStats = SellerStats(
        newOrders = orders.count { it.status == OrderStatus.ORDERED },
        inProgress = orders.count { it.status == OrderStatus.PACKED || it.status == OrderStatus.SHIPPED },
        delivered = orders.count { it.status == OrderStatus.DELIVERED },
        revenuePaise = orders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.revenueOf(sellerId) }
    )
}
