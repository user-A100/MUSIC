package com.gdstudio.music.next.download

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.gdstudio.music.next.data.Track
import java.io.File

/** Public output folder shared by audio and lyric files. */
const val DOWNLOAD_FOLDER = "GDMusic"

private val MIME_TYPES = mapOf(
    "mp3" to "audio/mpeg",
    "flac" to "audio/flac",
    "ogg" to "audio/ogg",
    "m4a" to "audio/mp4",
    "m4s" to "audio/mp4",
    "mp4" to "audio/mp4",
    "aac" to "audio/aac",
    "wav" to "audio/wav",
    "alac" to "audio/mp4",
    "aiff" to "audio/aiff",
    "aif" to "audio/aiff",
    "ape" to "audio/x-ape",
    "lrc" to "text/plain",
)

fun mimeTypeOf(extension: String): String = MIME_TYPES[extension] ?: "application/octet-stream"

/**
 * Writes finished downloads into the public `Music/GDMusic` folder.
 *
 * API 29+ goes through MediaStore with `IS_PENDING`, which needs no storage permission and lets
 * other players index the file. Older releases fall back to the public music directory, which is
 * why the manifest declares `WRITE_EXTERNAL_STORAGE` with `maxSdk="28"`.
 */
class MediaStoreSink(private val context: Context) {

    fun saveAudio(source: File, displayName: String, extension: String, track: Track): Uri {
        val mimeType = mimeTypeOf(extension)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveThroughMediaStore(source, displayName, mimeType, track)
        } else {
            saveThroughPublicDirectory(source, displayName, mimeType)
        }
    }

    fun saveLyric(source: File, displayName: String): Uri? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, mimeTypeOf("lrc"))
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/$DOWNLOAD_FOLDER")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return null
            resolver.openOutputStream(uri)?.use { output -> source.inputStream().use { it.copyTo(output) } }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } else {
            saveThroughPublicDirectory(source, displayName, mimeTypeOf("lrc"))
        }
    }.getOrNull()

    fun delete(uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }

    private fun saveThroughMediaStore(
        source: File,
        displayName: String,
        mimeType: String,
        track: Track,
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            put(MediaStore.Audio.Media.TITLE, track.name)
            put(MediaStore.Audio.Media.ARTIST, track.artistText)
            put(MediaStore.Audio.Media.ALBUM, track.album)
            put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/$DOWNLOAD_FOLDER")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("无法在公共音乐目录创建文件")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: throw IllegalStateException("无法写入公共音乐目录")
            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (error: Throwable) {
            delete(uri)
            throw error
        }
    }

    private fun saveThroughPublicDirectory(source: File, displayName: String, mimeType: String): Uri {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            DOWNLOAD_FOLDER,
        )
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IllegalStateException("无法创建下载目录 ${directory.absolutePath}")
        }
        val target = File(directory, displayName)
        source.inputStream().use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf(mimeType), null)
        return Uri.fromFile(target)
    }
}
