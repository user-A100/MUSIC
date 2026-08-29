package com.gdstudio.music.next.data

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.Gson
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Keep
data class LocalPlaylist(
    val id: String,
    val name: String,
    val tracks: List<Track> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

@Keep
data class LibraryState(
    val likedTracks: List<Track> = emptyList(),
    val playlists: List<LocalPlaylist> = emptyList(),
    val historyTracks: List<Track> = emptyList(),
) {
    val likedKeys: Set<String> get() = likedTracks.mapTo(linkedSetOf()) { it.libraryKey }
}

val Track.libraryKey: String get() = "$source:$id"

/** Small, local-first music library. Full track snapshots keep collections usable after restart. */
class LibraryRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val mutableState = MutableStateFlow(load())
    val state: StateFlow<LibraryState> = mutableState.asStateFlow()

    @Synchronized
    fun toggleLiked(track: Track) = update { current ->
        val tracks = if (track.libraryKey in current.likedKeys) {
            current.likedTracks.filterNot { it.libraryKey == track.libraryKey }
        } else {
            listOf(track) + current.likedTracks.filterNot { it.libraryKey == track.libraryKey }
        }
        current.copy(likedTracks = tracks.take(MAX_LIKED_TRACKS))
    }

    @Synchronized
    fun createPlaylist(name: String): String? {
        val cleanName = name.trim()
        if (cleanName.isBlank() || mutableState.value.playlists.any { it.name.equals(cleanName, true) }) return null
        val playlist = LocalPlaylist(id = UUID.randomUUID().toString(), name = cleanName)
        update { it.copy(playlists = it.playlists + playlist) }
        return playlist.id
    }

    @Synchronized
    fun deletePlaylist(playlistId: String) = update { current ->
        current.copy(playlists = current.playlists.filterNot { it.id == playlistId })
    }

    @Synchronized
    fun renamePlaylist(playlistId: String, name: String): Boolean {
        val cleanName = name.trim()
        if (cleanName.isBlank() || mutableState.value.playlists.any { it.id != playlistId && it.name.equals(cleanName, true) }) return false
        update { current ->
            current.copy(playlists = current.playlists.map { if (it.id == playlistId) it.copy(name = cleanName) else it })
        }
        return true
    }

    @Synchronized
    fun recordPlayed(track: Track) = update { current ->
        current.copy(historyTracks = (listOf(track) + current.historyTracks.filterNot { it.libraryKey == track.libraryKey }).take(MAX_HISTORY))
    }

    @Synchronized
    fun clearHistory() = update { it.copy(historyTracks = emptyList()) }

    @Synchronized
    fun addToPlaylist(playlistId: String, track: Track): Boolean {
        val playlist = mutableState.value.playlists.firstOrNull { it.id == playlistId } ?: return false
        if (playlist.tracks.any { it.libraryKey == track.libraryKey }) return false
        update { current ->
            current.copy(playlists = current.playlists.map {
                if (it.id == playlistId) it.copy(tracks = it.tracks + track) else it
            })
        }
        return true
    }

    @Synchronized
    fun removeFromPlaylist(playlistId: String, track: Track) = update { current ->
        current.copy(playlists = current.playlists.map {
            if (it.id == playlistId) it.copy(tracks = it.tracks.filterNot { item -> item.libraryKey == track.libraryKey }) else it
        })
    }

    private fun update(transform: (LibraryState) -> LibraryState) {
        val next = transform(mutableState.value)
        mutableState.value = next
        preferences.edit().putString(KEY_LIBRARY, gson.toJson(next)).apply()
    }

    private fun load(): LibraryState = preferences.getString(KEY_LIBRARY, null)
        ?.let { json -> runCatching { gson.fromJson(json, LibraryState::class.java) }.getOrNull() }
        ?: LibraryState()

    private companion object {
        const val PREFERENCES = "local_music_library"
        const val KEY_LIBRARY = "library_v1"
        const val MAX_LIKED_TRACKS = 2_000
        const val MAX_HISTORY = 500
    }
}
