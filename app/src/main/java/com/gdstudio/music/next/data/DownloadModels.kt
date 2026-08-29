package com.gdstudio.music.next.data

/**
 * Download-side models that mirror the public website behaviour.
 *
 * The site builds the download file name from `mkPlayer.nameformat` (0/1/2), strips the
 * character class `[/:*?"<>|=]` and infers the container from the audio URL, falling back
 * to `flac` when the requested bitrate is above 320 and `mp3` otherwise.
 */
enum class NameFormat(val displayName: String) {
    ARTIST_TITLE("歌手 - 歌曲"),
    TITLE_ARTIST("歌曲 - 歌手"),
    TITLE_ONLY("仅歌曲名"),
}

data class DownloadOptions(
    val bitrate: Int = DEFAULT_BITRATE,
    val nameFormat: NameFormat = NameFormat.ARTIST_TITLE,
    val embedMetadata: Boolean = true,
    val saveLyricFile: Boolean = true,
) {
    companion object {
        const val DEFAULT_BITRATE = 320
    }
}

data class DownloadCandidate(
    val track: Track,
    val url: String,
    val extension: String,
    val bitrate: Int,
    val sizeBytes: Long,
)

private val ILLEGAL_FILE_NAME_CHARS = Regex("[/:*?\"<>|=]")
private val AUDIO_EXTENSIONS = setOf(
    "mp3", "flac", "ogg", "m4a", "m4s", "mp4", "aac", "wav", "alac", "aiff", "aif", "ape",
)

/** Same rule as the website: text after the last dot of the query-less URL, whitelisted. */
fun audioExtension(url: String, bitrate: Int): String {
    val candidate = url.substringBefore('?').substringAfterLast('.')
    return candidate.takeIf { it in AUDIO_EXTENSIONS } ?: if (bitrate > 320) "flac" else "mp3"
}

fun downloadTitle(track: Track, format: NameFormat): String = when (format) {
    NameFormat.ARTIST_TITLE -> "${track.artistText} - ${track.name}"
    NameFormat.TITLE_ARTIST -> "${track.name} - ${track.artistText}"
    NameFormat.TITLE_ONLY -> track.name
}

fun sanitizeFileName(value: String): String {
    val cleaned = value.replace(ILLEGAL_FILE_NAME_CHARS, " ").trim().ifBlank { "untitled" }
    return cleaned.truncateToUtf8Bytes(180)
}

fun downloadFileName(track: Track, format: NameFormat, extension: String): String =
    "${sanitizeFileName(downloadTitle(track, format))}.$extension"

/**
 * Android file systems cap a single path element at 255 bytes, while Chinese titles already
 * cost three bytes per character, so the title is trimmed on the UTF-8 byte boundary.
 */
private fun String.truncateToUtf8Bytes(limit: Int): String {
    if (encodeToByteArray().size <= limit) return this
    val builder = StringBuilder()
    var bytes = 0
    for (char in this) {
        val width = char.toString().encodeToByteArray().size
        if (bytes + width > limit) break
        builder.append(char)
        bytes += width
    }
    return builder.toString().trim().ifBlank { "untitled" }
}
