package com.example.shopp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopp.AppUtil
import com.example.shopp.R
import com.example.shopp.viewmodel.AuthViewModel


@Composable
fun LoginScreen(modifier: Modifier = Modifier,navController : NavController, authViewModel : AuthViewModel = viewModel()){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var context = LocalContext.current

    val scope = rememberCoroutineScope()
    val webClientId = "605943827235-4e7mfr5n29ho44cs3lvldrj9octfusoh.apps.googleusercontent.com"

    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Welcome Back!",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Login to your account",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 22.sp,
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.signupbanner),
            contentDescription = "Banner",
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(value = email, onValueChange = {
            email = it
        },
            label = {
                Text(text = "Email")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(value = password, onValueChange = {
            password = it
        },
            label = {
                Text(text = "Password")
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            isLoading = true
            authViewModel.login(email, password){success,errorMessage->
                if(success){
                    isLoading = false
                    navController.navigate("home"){
                        popUpTo("auth"){ inclusive = true }
                    }
                }else{
                    isLoading = false
                    AppUtil.showToast(context = context, errorMessage?:"Something went wrong")
                }
            }
        },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
                .height(60.dp)
        ) {
            Text(text = if (isLoading) "Logging in..." else "Login", fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("or", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                isLoading = true
                authViewModel.googleSignIn(context, scope, webClientId) { success, error ->
                    isLoading = false
                    if (success) {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        AppUtil.showToast(context, error ?: "Google Sign-In failed")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google), // add google icon to drawable
                contentDescription = "Google",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continue with Google")
        }
    }
}