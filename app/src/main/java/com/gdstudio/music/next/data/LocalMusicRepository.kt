package com.gdstudio.music.next.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMusicRepository(private val context: Context) {
    suspend fun scan(): List<Track> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )
        val tracks = mutableListOf<Track>()
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id).toString()
                tracks += Track(
                    id = id.toString(),
                    name = cursor.getString(titleColumn) ?: "未知歌曲",
                    artists = listOf(cursor.getString(artistColumn)?.takeUnless { it == "<unknown>" } ?: "未知歌手"),
                    album = cursor.getString(albumColumn)?.takeUnless { it == "<unknown>" } ?: "本地音乐",
                    source = LOCAL_SOURCE,
                    urlId = uri,
                    pictureId = null,
                    lyricId = null,
                    durationSeconds = (cursor.getLong(durationColumn) / 1000L).toInt(),
                    hasHiRes = false,
                )
            }
        }
        tracks
    }

    companion object { const val LOCAL_SOURCE = "local" }
}
