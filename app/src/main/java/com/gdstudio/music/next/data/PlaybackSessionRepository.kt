package com.gdstudio.music.next.data

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.Gson

@Keep
data class PlaybackSession(
    val queue: List<Track> = emptyList(),
    val currentTrack: Track? = null,
    val positionMs: Long = 0L,
)

class PlaybackSessionRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): PlaybackSession = preferences.getString(KEY_SESSION, null)
        ?.let { runCatching { gson.fromJson(it, PlaybackSession::class.java) }.getOrNull() }
        ?: PlaybackSession()

    fun save(session: PlaybackSession) {
        preferences.edit().putString(KEY_SESSION, gson.toJson(session)).apply()
    }

    private companion object {
        const val PREFERENCES = "playback_session"
        const val KEY_SESSION = "session_v1"
    }
}
