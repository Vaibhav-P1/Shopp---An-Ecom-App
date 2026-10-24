package com.example.shopp.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** The last few search queries, newest first, kept on the device. */
@Singleton
class RecentSearchesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)

    private val _searches = MutableStateFlow(load())
    val searches: StateFlow<List<String>> = _searches.asStateFlow()

    fun add(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        save(listOf(trimmed) + _searches.value.filterNot { it.equals(trimmed, ignoreCase = true) })
    }

    fun remove(query: String) = save(_searches.value - query)

    fun clear() = save(emptyList())

    private fun save(items: List<String>) {
        val limited = items.take(MAX_ITEMS)
        _searches.value = limited
        prefs.edit().putString(KEY, limited.joinToString(SEPARATOR)).apply()
    }

    private fun load(): List<String> =
        prefs.getString(KEY, "").orEmpty().split(SEPARATOR).filter { it.isNotBlank() }

    private companion object {
        const val KEY = "queries"
        const val SEPARATOR = "\n"
        const val MAX_ITEMS = 8
    }
}
