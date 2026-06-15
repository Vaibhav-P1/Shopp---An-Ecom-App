package com.example.shopp.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.components.BannerView
import com.example.shopp.components.CategoriesView
import com.example.shopp.components.HeaderView
import com.example.shopp.components.OfferCard
import com.example.shopp.model.OfferModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomePage(modifier: Modifier = Modifier) {

    var offerList by remember { mutableStateOf<List<OfferModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("offers")

            .get()
            .addOnSuccessListener { snapshot ->
                offerList = snapshot.documents
                    .mapNotNull { doc ->
                        doc.toObject(OfferModel::class.java)?.copy(id = doc.id)
                    }
                    .sortedBy { it.priority }
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(8.dp))
            HeaderView(modifier)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            BannerView(modifier = Modifier.height(170.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(text = "Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            CategoriesView()
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(text = "🔥 Deals & Offers", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(offerList) { offer ->
            OfferCard(offer = offer, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}