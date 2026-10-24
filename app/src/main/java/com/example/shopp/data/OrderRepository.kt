package com.example.shopp.data

import com.example.shopp.domain.CouponResult
import com.example.shopp.domain.CouponRules
import com.example.shopp.domain.Money
import com.example.shopp.domain.PriceCalculator
import com.example.shopp.model.AddressModel
import com.example.shopp.model.OrderLineModel
import com.example.shopp.model.OrderModel
import com.example.shopp.model.OrderStatus
import com.example.shopp.model.ProductModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

enum class PaymentMethod { COD, ONLINE }

@Singleton
class OrderRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Places the order for everything currently in the user's cart, atomically:
     * prices are re-read from the catalogue, the order is written with a snapshot of each line and of the
     * saved address named by [addressId], and the cart is cleared. A [couponCode] is re-validated here against the
     * stored coupon, so a stale or tampered client can never get a discount it is not entitled to. Calling it again with the same [orderId]
     * is a no-op, so a duplicated payment callback or a double tap can never create a second order.
     */
    suspend fun placeOrder(
        orderId: String,
        addressId: String,
        couponCode: String?,
        paymentMethod: PaymentMethod,
        paymentId: String?
    ): Result<Unit> =
        firestoreCall {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in first")
            val userRef = firestore.collection("users").document(uid)
            val orderRef = firestore.collection("orders").document(orderId)
            val products = firestore.collection("data").document("stock").collection("products")

            firestore.runTransaction { transaction ->
                // All reads first: a transaction may not read after it has written
                if (transaction.get(orderRef).exists()) return@runTransaction Unit

                val user = transaction.get(userRef)
                val cart = parseQuantities(user.get("cartItems"))
                if (cart.isEmpty()) throw IllegalStateException("Your cart is empty")

                val couponSnapshot = couponCode?.takeIf { it.isNotBlank() }?.let {
                    transaction.get(firestore.collection(CouponRepository.COLLECTION).document(CouponRepository.normalize(it)))
                }
                val address = transaction.get(userRef.collection("addresses").document(addressId))
                    .toObject(AddressModel::class.java)
                    ?: throw IllegalStateException("Choose a delivery address before placing an order")

                val coupon = couponSnapshot?.let {
                    it.toCoupon() ?: throw IllegalStateException("Invalid coupon code")
                }

                // Products whose stock is tracked: (reference, stock before, quantity ordered)
                val stockUpdates = mutableListOf<Triple<DocumentReference, Long?, Long>>()
                val lines = cart.map { (productId, quantity) ->
                    val snapshot = transaction.get(products.document(productId))
                    val product = snapshot.toObject(ProductModel::class.java)
                        ?: throw IllegalStateException("A product in your cart is no longer available")
                    val unitPrice = Money.parsePaise(product.actualPrice)
                        ?: throw IllegalStateException("${product.title} has no valid price")
                    if (!product.active) throw IllegalStateException("${product.title} is no longer available")
                    product.stock?.let { stock ->
                        if (quantity > stock) {
                            throw IllegalStateException(
                                if (stock <= 0) "${product.title} is out of stock" else "Only $stock left of ${product.title}"
                            )
                        }
                    }
                    stockUpdates += Triple(snapshot.reference, product.stock, quantity)
                    OrderLineModel(
                        productId = productId,
                        title = product.title,
                        imageUrl = product.images.firstOrNull().orEmpty(),
                        unitPricePaise = unitPrice,
                        quantity = quantity,
                        sellerId = product.sellerId
                    )
                }
                val subtotal = lines.sumOf { it.unitPricePaise * it.quantity }
                val discount = coupon?.let { applied ->
                    when (val result = CouponRules.evaluate(applied, subtotal, System.currentTimeMillis())) {
                        is CouponResult.Applied -> result.discountPaise
                        is CouponResult.Rejected -> throw IllegalStateException(result.reason)
                    }
                } ?: 0L
                val breakdown = PriceCalculator.calculate(subtotal, discount)

                transaction.set(
                    orderRef,
                    OrderModel(
                        id = orderId,
                        date = Timestamp.now(),
                        userId = uid,
                        items = cart,
                        status = OrderStatus.ORDERED,
                        address = address.formatted,
                        lines = lines,
                        subtotalPaise = breakdown.subtotalPaise,
                        discountPaise = breakdown.discountPaise,
                        couponCode = coupon?.code.orEmpty(),
                        sellerIds = lines.map { it.sellerId }.filter { it.isNotBlank() }.distinct(),
                        taxPaise = breakdown.taxPaise,
                        totalPaise = breakdown.totalPaise,
                        paymentMethod = paymentMethod.name,
                        paymentId = paymentId.orEmpty()
                    )
                )
                stockUpdates.forEach { (ref, stock, quantity) ->
                    if (stock != null) transaction.update(ref, "stock", stock - quantity)
                }
                transaction.update(userRef, "cartItems", FieldValue.delete())
                Unit
            }.await()
            Unit
        }

    /** Live list of the signed-in user's orders, newest first. Fails the flow if the listener errors. */
    fun observeOrders(): Flow<List<OrderModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = firestore.collection("orders")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents
                    ?.mapNotNull { it.toObject(OrderModel::class.java) }
                    ?.sortedByDescending { it.date }
                    .orEmpty()
                trySend(orders)
            }
        awaitClose { registration.remove() }
    }

    /** Live list of orders that contain the signed-in seller's products, newest first. */
    fun observeSellerOrders(): Flow<List<OrderModel>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = firestore.collection("orders")
            .whereArrayContains("sellerIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents
                    ?.mapNotNull { it.toObject(OrderModel::class.java) }
                    ?.sortedByDescending { it.date }
                    .orEmpty()
                trySend(orders)
            }
        awaitClose { registration.remove() }
    }

    /** Moves an order one step forward (Placed -> Packed -> Shipped -> Delivered). Firestore rules allow it for sellers only. */
    suspend fun advanceStatus(orderId: String): Result<Unit> = firestoreCall {
        val orderRef = firestore.collection("orders").document(orderId)
        firestore.runTransaction { transaction ->
            val current = transaction.get(orderRef).getString("status").orEmpty()
            val next = OrderStatus.next(current) ?: throw IllegalStateException("This order can no longer be updated")
            transaction.update(orderRef, mapOf("status" to next, "statusUpdatedAt" to Timestamp.now()))
            Unit
        }.await()
        Unit
    }

    /** Live view of one order, so status changes made elsewhere show up while the screen is open. */
    fun observeOrder(orderId: String): Flow<OrderModel> = callbackFlow {
        val registration = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(OrderModel::class.java)
                if (order == null) close(IllegalStateException("Order not found")) else trySend(order)
            }
        awaitClose { registration.remove() }
    }

    /** Cancels an order that has not been packed yet. Later states are handled by the seller, not the client. */
    suspend fun cancelOrder(orderId: String): Result<Unit> = firestoreCall {
        val orderRef = firestore.collection("orders").document(orderId)
        firestore.runTransaction { transaction ->
            val status = transaction.get(orderRef).getString("status")
            if (status != OrderStatus.ORDERED) throw IllegalStateException("This order can no longer be cancelled")
            transaction.update(orderRef, mapOf("status" to OrderStatus.CANCELLED, "cancelledAt" to Timestamp.now()))
            Unit
        }.await()
        Unit
    }

    private fun parseQuantities(raw: Any?): Map<String, Long> =
        (raw as? Map<*, *>)
            ?.mapNotNull { (key, value) ->
                val productId = key as? String ?: return@mapNotNull null
                val quantity = (value as? Number)?.toLong() ?: return@mapNotNull null
                productId to quantity
            }
            ?.toMap()
            .orEmpty()
}
