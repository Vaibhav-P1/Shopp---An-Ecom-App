package com.example.shopp.ui

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/** Human readable date and time, e.g. "27 May 2025 04:30 PM". */
fun Timestamp.formatDate(): String =
    SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(toDate())
