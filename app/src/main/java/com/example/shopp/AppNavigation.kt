package com.example.shopp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopp.pages.CategoryProductsPage
import com.example.shopp.screen.AuthScreen
import com.example.shopp.screen.HomeScreen
import com.example.shopp.screen.LoginScreen
import com.example.shopp.screen.SignupScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier){

    val navController = rememberNavController()
    GlobalNavigation.navController = navController

    val isLoggedIn = Firebase.auth.currentUser != null
    val firstPage = if(isLoggedIn) "home" else "auth"

    NavHost(navController = navController, startDestination = firstPage){

        composable("auth"){
            AuthScreen(modifier,navController)
        }

        composable("login"){
            LoginScreen(modifier,navController)
        }

        composable("signup"){
            SignupScreen(modifier,navController)
        }

        composable("home"){
            HomeScreen(modifier,navController)
        }

        composable("category-products/{categoryId}"){
            var categoryId = it.arguments?.getString("categoryId")
            CategoryProductsPage(modifier,categoryId?:"")
        }

    }
}

object GlobalNavigation{
    lateinit var navController: NavHostController
}