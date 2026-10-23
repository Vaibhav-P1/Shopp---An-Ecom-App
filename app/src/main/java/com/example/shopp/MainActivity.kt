package com.example.shopp

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.shopp.ui.theme.ShoppTheme
import com.razorpay.PaymentResultListener

class MainActivity : ComponentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // must run before super.onCreate
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onPaymentSuccess(p0: String?) {
//        AppUtil.showToast(this,"Payment Successful")

        AppUtil.clearCartAndAddToOrders()

        // Construct interactive UX confirmation prompt modal blocks
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Payment Successful")
        .setMessage("Thank you! Your payment was completed successfully, and your order has been placed.")

        .setPositiveButton("OK") { _ , _ ->
            val navController = GlobalNavigation.navController
            // Wipe the back stack so Back can't return to checkout
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
        .setCancelable(false)
            .show()
//        confirmationDialog.show()

    }

    override fun onPaymentError(p0: Int, p1: String?) {
        AppUtil.showToast(this,"Payment Failed")
    }
}



