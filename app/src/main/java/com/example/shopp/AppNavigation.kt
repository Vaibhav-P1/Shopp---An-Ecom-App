package com.example.shopp

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopp.pages.AddressBookPage
import com.example.shopp.pages.CategoryProductsPage
import com.example.shopp.pages.CheckoutPage
import com.example.shopp.pages.OrderDetailsPage
import com.example.shopp.pages.OrdersPage
import com.example.shopp.pages.SearchPage
import com.example.shopp.pages.ProductDetailsPage
import com.example.shopp.screen.AuthScreen
import com.example.shopp.screen.HomeScreen
import com.example.shopp.screen.LoginScreen
import com.example.shopp.screen.OnboardingScreen
import com.example.shopp.screen.OrderPlacedScreen
import com.example.shopp.screen.SignupScreen
import com.example.shopp.seller.SellerScreen

@Composable
fun AppNavigation(isLoggedIn: Boolean, onboardingSeen: Boolean, onOnboardingFinished: () -> Unit) {

    // Full-screen destinations keep clear of the system bars and keyboard. Auth screens handle
    // this themselves (scrollable layout) and Home's tab Scaffold already applies the bars.
    val insets = Modifier.safeDrawingPadding()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = when {
            isLoggedIn -> Home
            onboardingSeen -> Auth
            else -> Onboarding
        }
    ) {

        composable<Onboarding> {
            OnboardingScreen(onFinish = {
                onOnboardingFinished()
                navController.navigate(Auth) { popUpTo<Onboarding> { inclusive = true } }
            })
        }

        composable<Auth> {
            AuthScreen(
                onLoginClick = { navController.navigate(Login) },
                onSignupClick = { navController.navigate(Signup) },
                onAuthenticated = { navController.goHomeAfterAuth() }
            )
        }

        composable<Login> {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onSignupClick = { navController.navigate(Signup) { popUpTo<Login> { inclusive = true } } },
                onAuthenticated = { navController.goHomeAfterAuth() }
            )
        }

        composable<Signup> {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onLoginClick = { navController.navigate(Login) { popUpTo<Signup> { inclusive = true } } },
                onAuthenticated = { navController.goHomeAfterAuth() }
            )
        }

        composable<Home> {
            HomeScreen(
                onCategoryClick = { navController.navigate(CategoryProducts(it)) },
                onProductClick = { navController.navigate(ProductDetails(it)) },
                onCheckout = { navController.navigate(Checkout) },
                onOrdersClick = { navController.navigate(Orders) },
                onSellerClick = { navController.navigate(Seller) },
                onSearchClick = { navController.navigate(Search) },
                onAddressesClick = { navController.navigate(AddressBook) },
                onSignedOut = {
                    // Clear the whole back stack so Back can't return into the signed-out app
                    navController.navigate(Auth) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable<AddressBook> {
            AddressBookPage(modifier = insets, onBack = { navController.popBackStack() })
        }

        composable<Search> {
            SearchPage(
                modifier = insets,
                onBack = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate(ProductDetails(productId)) }
            )
        }

        composable<CategoryProducts> {
            CategoryProductsPage(
                modifier = insets,
                onBack = { navController.popBackStack() },
                onSearchClick = { navController.navigate(Search) },
                onProductClick = { productId -> navController.navigate(ProductDetails(productId)) }
            )
        }

        composable<ProductDetails> {
            ProductDetailsPage(
                onBack = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate(ProductDetails(productId)) }
            )
        }

        composable<Checkout> {
            CheckoutPage(
                modifier = insets,
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    // Replace checkout with the confirmation so Back can't return to it
                    navController.navigate(OrderPlaced(orderId)) { popUpTo<Checkout> { inclusive = true } }
                },
                onManageAddresses = { navController.navigate(AddressBook) }
            )
        }

        composable<OrderPlaced> {
            OrderPlacedScreen(
                onTrackOrder = { orderId ->
                    navController.navigate(OrderDetails(orderId)) { popUpTo<OrderPlaced> { inclusive = true } }
                },
                onContinueShopping = { navController.popBackStack<Home>(inclusive = false) }
            )
        }

        composable<Orders> {
            OrdersPage(
                modifier = insets,
                onBack = { navController.popBackStack() },
                onOrderClick = { orderId -> navController.navigate(OrderDetails(orderId)) }
            )
        }

        composable<OrderDetails> {
            OrderDetailsPage(modifier = insets, onBack = { navController.popBackStack() })
        }

        composable<Seller> {
            SellerScreen(onExit = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.goHomeAfterAuth() {
    navigate(Home) { popUpTo<Auth> { inclusive = true } }
}
