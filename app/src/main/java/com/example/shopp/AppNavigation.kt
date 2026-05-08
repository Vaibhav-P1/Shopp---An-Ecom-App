package com.example.shopp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopp.screen.AuthScreen
import com.example.shopp.screen.LoginScreen
import com.example.shopp.screen.SignupScreen

@Composable
fun AppNavigation(modifier: Modifier){

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth"){

        composable("auth"){
            AuthScreen(modifier,navController)
        }

        composable("login"){
            LoginScreen(modifier)
        }

        composable("signup"){
            SignupScreen(modifier)
        }


    }
}