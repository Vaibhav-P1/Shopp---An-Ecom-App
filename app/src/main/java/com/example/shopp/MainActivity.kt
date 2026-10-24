package com.example.shopp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.shopp.data.AuthRepository
import com.example.shopp.data.OnboardingRepository
import com.example.shopp.data.ThemePreferenceRepository
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.payment.PaymentResult
import com.example.shopp.payment.PaymentResultBus
import com.example.shopp.screen.BrandSplash
import com.example.shopp.ui.theme.ShoppTheme
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener {

    @Inject lateinit var paymentResultBus: PaymentResultBus
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var onboardingRepository: OnboardingRepository
    @Inject lateinit var themePreferences: ThemePreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // must run before super.onCreate
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkChoice by themePreferences.dark.collectAsStateWithLifecycle()
            val darkTheme = darkChoice ?: isSystemInDarkTheme()
            // Keep the system bar icons readable when the in-app choice differs from the system theme
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                        darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
                    ) { darkTheme }
                )
                onDispose { }
            }
            ShoppTheme(darkTheme = darkTheme) {
                // Insets are handled per screen (see AppNavigation), so there is no app-level Scaffold
                var showSplash by rememberSaveable { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(SPLASH_MILLIS)
                    showSplash = false
                }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Crossfade(targetState = showSplash, label = "splash") { splash ->
                        if (splash) BrandSplash() else AppNavigation(
                            isLoggedIn = authRepository.isLoggedIn,
                            onboardingSeen = onboardingRepository.isSeen,
                            onOnboardingFinished = onboardingRepository::markSeen
                        )
                    }
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

    private companion object {
        const val SPLASH_MILLIS = 1200L
    }
}
