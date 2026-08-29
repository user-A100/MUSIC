package com.gdstudio.music.next.data

import android.content.Context

class SourcePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun loadOrder(): List<MusicSource> {
        val saved = preferences.getString(KEY_ORDER, null)
            ?.split(',')
            ?.mapNotNull { id -> MusicSource.entries.firstOrNull { it.id == id } }
            .orEmpty()
        return (saved + MusicSource.defaultPriority + MusicSource.entries).distinct()
    }

    fun loadHidden(): Set<MusicSource> {
        val ids = preferences.getStringSet(KEY_HIDDEN, emptySet()).orEmpty()
        return MusicSource.entries.filterTo(mutableSetOf()) { it.id in ids }
    }

    fun save(order: List<MusicSource>, hidden: Set<MusicSource>) {
        preferences.edit()
            .putString(KEY_ORDER, order.joinToString(",") { it.id })
            .putStringSet(KEY_HIDDEN, hidden.mapTo(mutableSetOf()) { it.id })
            .apply()
    }

    companion object {
        private const val FILE_NAME = "source_preferences"
        private const val KEY_ORDER = "order"
        private const val KEY_HIDDEN = "hidden"
    }
}
