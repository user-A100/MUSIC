package com.gdstudio.music.next.data

import androidx.annotation.Keep

enum class MusicSource(val id: String, val displayName: String) {
    NETEASE("netease", "网易云"),
    TENCENT("tencent", "QQ 音乐"),
    KUWO("kuwo", "酷我"),
    TIDAL("tidal", "Tidal"),
    QOBUZ("qobuz", "Qobuz"),
    JOOX("joox", "JOOX"),
    BILIBILI("bilibili", "B 站"),
    APPLE("apple", "Apple Music"),
    YOUTUBE("ytmusic", "YouTube Music"),
    SPOTIFY("spotify", "Spotify"),

    ;

    val availability: SourceAvailability
        get() = when (this) {
            NETEASE, JOOX, BILIBILI -> SourceAvailability.STABLE
            KUWO -> SourceAvailability.SEARCH_ONLY
            TENCENT -> SourceAvailability.UNAVAILABLE
            else -> SourceAvailability.UNSTABLE
        }

    companion object {
        val defaultPriority = listOf(
            NETEASE, JOOX, BILIBILI, KUWO, TENCENT,
            TIDAL, QOBUZ, APPLE, YOUTUBE, SPOTIFY,
        )
    }
}

enum class SourceAvailability(val label: String) {
    STABLE("当前稳定"),
    SEARCH_ONLY("可搜索，解析可能不可用"),
    UNAVAILABLE("当前未开放"),
    UNSTABLE("可用性不稳定"),
}

@Keep
data class Track(
    val id: String,
    val name: String,
    val artists: List<String>,
    val album: String,
    val source: String,
    val urlId: String,
    val pictureId: String?,
    val lyricId: String?,
    val durationSeconds: Int,
    val hasHiRes: Boolean,
) {
    val artistText: String get() = artists.joinToString(", ")
}

data class ResolvedTrack(
    val track: Track,
    val audioUrl: String,
    val artworkUrl: String?,
    val lyric: String,
    val bitrate: Int,
    val sizeBytes: Long = 0L,
)

/** Playable/downloadable link returned by `types=url`. */
data class AudioLink(
    val url: String,
    val bitrate: Int,
    val sizeBytes: Long,
)

data class LyricLine(
    val timestampMs: Long,
    val text: String,
)

fun parseLrc(raw: String): List<LyricLine> {
    val timePattern = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")
    return raw.lineSequence().flatMap { line ->
        val text = line.replace(timePattern, "").trim()
        timePattern.findAll(line).map { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: 0L
            val seconds = match.groupValues[2].toLongOrNull() ?: 0L
            val fraction = match.groupValues[3]
            val millis = when (fraction.length) {
                1 -> (fraction.toLongOrNull() ?: 0L) * 100L
                2 -> (fraction.toLongOrNull() ?: 0L) * 10L
                else -> fraction.take(3).padEnd(3, '0').toLongOrNull() ?: 0L
            }
            LyricLine((minutes * 60L + seconds) * 1_000L + millis, text)
        }
    }.filter { it.text.isNotBlank() }.sortedBy { it.timestampMs }.toList()
}
