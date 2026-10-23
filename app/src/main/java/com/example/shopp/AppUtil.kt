package com.example.shopp

import android.content.Context
import android.widget.Toast
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun formatDate(timestamp: Timestamp): String {
        // Formats down into human readable values (e.g., "27 May 2025 04:30 PM")
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
        return simpleDateFormat.format(timestamp.toDate().time)
    }

}
