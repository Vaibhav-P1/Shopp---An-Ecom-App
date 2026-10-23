package com.example.shopp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopp.pages.CategoryProductsPage
import com.example.shopp.pages.CheckoutPage
import com.example.shopp.pages.OrderDetailsPage
import com.example.shopp.pages.OrdersPage
import com.example.shopp.pages.ProductDetailsPage
import com.example.shopp.screen.AuthScreen
import com.example.shopp.screen.HomeScreen
import com.example.shopp.screen.LoginScreen
import com.example.shopp.screen.SignupScreen
import com.example.shopp.seller.SellerAccount

@Composable
fun AppNavigation(modifier: Modifier, isLoggedIn: Boolean) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = if (isLoggedIn) Home else Auth) {

        composable<Auth> {
            AuthScreen(
                modifier = modifier,
                onLoginClick = { navController.navigate(Login) },
                onSignupClick = { navController.navigate(Signup) },
                onAuthenticated = { navController.goHomeAfterAuth() }
            )
        }

        composable<Login> {
            LoginScreen(modifier = modifier, onAuthenticated = { navController.goHomeAfterAuth() })
        }

        composable<Signup> {
            SignupScreen(modifier = modifier, onAuthenticated = { navController.goHomeAfterAuth() })
        }

        composable<Home> {
            HomeScreen(
                modifier = modifier,
                onCategoryClick = { navController.navigate(CategoryProducts(it)) },
                onProductClick = { navController.navigate(ProductDetails(it)) },
                onCheckout = { navController.navigate(Checkout) },
                onOrdersClick = { navController.navigate(Orders) },
                onSellerClick = { navController.navigate(Seller) },
                onSignedOut = {
                    // Clear the whole back stack so Back can't return into the signed-out app
                    navController.navigate(Auth) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable<CategoryProducts> {
            CategoryProductsPage(
                modifier = modifier,
                onProductClick = { productId -> navController.navigate(ProductDetails(productId)) }
            )
        }

        composable<ProductDetails> {
            ProductDetailsPage(modifier)
        }

        composable<Checkout> {
            CheckoutPage(
                modifier = modifier,
                onOrderPlaced = {
                    // Wipe the back stack so Back can't return to checkout
                    navController.navigate(Home) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable<Orders> {
            OrdersPage(
                modifier = modifier,
                onOrderClick = { orderId -> navController.navigate(OrderDetails(orderId)) }
            )
        }

        composable<OrderDetails> {
            OrderDetailsPage(modifier)
        }

        composable<Seller> {
            SellerAccount(modifier)
        }
    }
}

private fun NavHostController.goHomeAfterAuth() {
    navigate(Home) { popUpTo<Auth> { inclusive = true } }
}
