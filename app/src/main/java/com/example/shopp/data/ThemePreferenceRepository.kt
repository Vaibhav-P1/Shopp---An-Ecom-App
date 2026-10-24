package com.example.shopp.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** The user's dark-mode choice. [dark] is null until they pick one, which means follow the system. */
@Singleton
class ThemePreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("theme", Context.MODE_PRIVATE)

    private val _dark = MutableStateFlow(load())
    val dark: StateFlow<Boolean?> = _dark.asStateFlow()

    fun setDark(enabled: Boolean) {
        _dark.value = enabled
        prefs.edit().putBoolean(KEY_DARK, enabled).apply()
    }

    private fun load(): Boolean? = if (prefs.contains(KEY_DARK)) prefs.getBoolean(KEY_DARK, false) else null

    private companion object {
        const val KEY_DARK = "dark"
    }
}
