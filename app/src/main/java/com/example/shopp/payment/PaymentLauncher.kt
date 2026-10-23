package com.example.shopp.payment

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.example.shopp.BuildConfig
import com.razorpay.Checkout
import org.json.JSONObject

/** Opens Razorpay checkout (test mode) for an amount in paise. */
object PaymentLauncher {

    fun start(activity: Activity, amountPaise: Long, orderId: String) {
        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY)

        val options = JSONObject().apply {
            put("name", "Shopp")
            put("description", "Order $orderId")
            put("amount", amountPaise)
            put("currency", "INR")
            put("receipt", orderId)
        }
        checkout.open(activity, options)
    }
}

fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
