package com.example.shopp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.AppUtil
import com.example.shopp.GlobalNavigation
import com.example.shopp.R
import com.example.shopp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



@Composable
fun ProfilePage(modifier: Modifier = Modifier) {

    var userModel = remember { mutableStateOf(UserModel()) }
    var addressInput by remember { mutableStateOf(userModel.value.address) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val result = it.result.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result
                        addressInput = userModel.value.address

                    }
                }
            }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .statusBarsPadding()
        ) {
            Text(
                text = "Your Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Avatar Render (expects profile_icon drawable resource asset)
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile Icon",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username Render Text line
            Text(
                text = userModel.value.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Address", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            TextField(
                value = addressInput,
                onValueChange = { addressInput = it },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    //Update to Firestore
                    if (addressInput.isNotEmpty()) {
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@KeyboardActions
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUserId)
                            .update("address", addressInput)
                            .addOnSuccessListener {
                                AppUtil.showToast(context, "Address updated successfully")
                            }
                    } else {
                        AppUtil.showToast(context, "Address can't be empty")
                    }
                })
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Immutable email reference text line
            Text(text = "Email:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(text = userModel.value.email, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(10.dp))

            // Total Accumulated quantities sum output display metric
            val totalCartItemsCount = userModel.value.cartItems.values.sum()
            Text(text = "Number of items in cart", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(text = "$totalCartItemsCount items", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Gateway to orders list records
            Text(
                text = "View My Orders",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        GlobalNavigation.navController.navigate("orders")
                    }
                    .padding(vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Core App Sign Out Controls Button Row Actions
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    val navController = GlobalNavigation.navController
                    // Clear active backstack metrics history parameters cleanly on logout tasks execution
                    navController.popBackStack()
                    navController.navigate("auth") // Maps redirect target back onto application initialization views
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Sign Out", fontSize = 18.sp)
            }


        }


}