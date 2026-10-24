package com.example.shopp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.shopp.data.AuthRepository
import com.example.shopp.payment.PaymentResult
import com.example.shopp.payment.PaymentResultBus
import com.example.shopp.ui.theme.ShoppTheme
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener {

    @Inject lateinit var paymentResultBus: PaymentResultBus
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // must run before super.onCreate
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppTheme {
                // Insets are handled per screen (see AppNavigation), so there is no app-level Scaffold
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(isLoggedIn = authRepository.isLoggedIn)
                }
            }
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        paymentResultBus.publish(PaymentResult.Success(paymentId.orEmpty()))
    }

    override fun onPaymentError(code: Int, message: String?) {
        paymentResultBus.publish(PaymentResult.Failure("Payment failed" + message?.let { ": $it" }.orEmpty()))
    }
}
