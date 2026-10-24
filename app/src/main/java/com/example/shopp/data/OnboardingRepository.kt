package com.example.shopp.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers on the device whether the first-run walkthrough has been seen. */
@Singleton
class OnboardingRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    val isSeen: Boolean get() = prefs.getBoolean(KEY_SEEN, false)

    fun markSeen() = prefs.edit().putBoolean(KEY_SEEN, true).apply()

    private companion object {
        const val KEY_SEEN = "seen"
    }
}
