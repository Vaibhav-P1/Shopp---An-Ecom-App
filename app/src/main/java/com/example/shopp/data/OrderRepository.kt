package com.example.shopp.data

import com.example.shopp.domain.Money
import com.example.shopp.domain.PriceCalculator
import com.example.shopp.model.OrderLineModel
import com.example.shopp.model.OrderModel
import com.example.shopp.model.ProductModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
     * prices are re-read from the catalogue, the order is written with a snapshot of each line,
     * and the cart is cleared. Calling it again with the same [orderId] is a no-op, so a duplicated
     * payment callback or a double tap can never create a second order.
     */
    suspend fun placeOrder(orderId: String, paymentMethod: PaymentMethod, paymentId: String?): Result<Unit> =
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

                val address = user.getString("address").orEmpty()
                if (address.isBlank()) throw IllegalStateException("Add a delivery address before placing an order")

                val lines = cart.map { (productId, quantity) ->
                    val snapshot = transaction.get(products.document(productId))
                    val product = snapshot.toObject(ProductModel::class.java)
                        ?: throw IllegalStateException("A product in your cart is no longer available")
                    val unitPrice = Money.parsePaise(product.actualPrice)
                        ?: throw IllegalStateException("${product.title} has no valid price")
                    OrderLineModel(
                        productId = productId,
                        title = product.title,
                        imageUrl = product.images.firstOrNull().orEmpty(),
                        unitPricePaise = unitPrice,
                        quantity = quantity
                    )
                }
                val breakdown = PriceCalculator.calculate(lines.sumOf { it.unitPricePaise * it.quantity })

                transaction.set(
                    orderRef,
                    OrderModel(
                        id = orderId,
                        date = Timestamp.now(),
                        userId = uid,
                        items = cart,
                        status = "ORDERED",
                        address = address,
                        lines = lines,
                        subtotalPaise = breakdown.subtotalPaise,
                        discountPaise = breakdown.discountPaise,
                        taxPaise = breakdown.taxPaise,
                        totalPaise = breakdown.totalPaise,
                        paymentMethod = paymentMethod.name,
                        paymentId = paymentId.orEmpty()
                    )
                )
                transaction.update(userRef, "cartItems", FieldValue.delete())
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
