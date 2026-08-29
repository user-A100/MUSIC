package com.gdstudio.music.next.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class GdMusicApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .build(),
) {
    suspend fun search(
        query: String,
        source: MusicSource,
        page: Int = 1,
        count: Int = 20,
    ): List<Track> {
        val json = get(
            "types" to "search",
            "count" to count.toString(),
            "source" to source.id,
            "pages" to page.toString(),
            "name" to query.trim(),
        )
        serviceError(json)?.let { throw IOException(sourceError(source, it)) }
        return GdMusicJsonParser.parseTracks(json)
    }

    suspend fun resolve(
        track: Track,
        bitrate: Int,
        fallbackSources: List<MusicSource> = emptyList(),
    ): ResolvedTrack = coroutineScope {
        val (playableTrack, link) = resolvePlayable(track, bitrate, fallbackSources)
        val picture = async { resolvePicture(playableTrack) }
        val lyric = async { resolveLyric(playableTrack) }
        ResolvedTrack(
            track = playableTrack,
            audioUrl = link.url,
            artworkUrl = picture.await(),
            lyric = lyric.await(),
            bitrate = link.bitrate,
            sizeBytes = link.sizeBytes,
        )
    }

    /**
     * Resolves the direct file link used by downloads.
     *
     * The website exposes the same link through `types=url` and additionally reports `size`,
     * which the download UI shows before the transfer starts.
     */
    suspend fun resolveDownload(
        track: Track,
        bitrate: Int,
        fallbackSources: List<MusicSource> = emptyList(),
    ): DownloadCandidate {
        val (playableTrack, link) = resolvePlayable(track, bitrate, fallbackSources)
        return DownloadCandidate(
            track = playableTrack,
            url = link.url,
            extension = audioExtension(link.url, link.bitrate),
            bitrate = link.bitrate,
            sizeBytes = link.sizeBytes,
        )
    }

    suspend fun resolveLyricText(track: Track): String = runCatching { resolveLyric(track) }.getOrDefault("")

    private suspend fun resolvePlayable(
        track: Track,
        bitrate: Int,
        fallbackSources: List<MusicSource>,
    ): Pair<Track, AudioLink> {
        val direct = runCatching { resolveUrl(track, bitrate) }
        direct.getOrNull()?.let { return track to it }

        fallbackSources.distinct().filterNot { it.id == track.source }.forEach { source ->
            val candidates = runCatching { search(track.name, source, count = 5) }.getOrDefault(emptyList())
            val candidate = candidates.firstOrNull { it.matches(track, requireArtist = true) }
                ?: candidates.firstOrNull { it.matches(track, requireArtist = false) }
                ?: return@forEach
            runCatching { resolveUrl(candidate, bitrate) }.getOrNull()?.let { return candidate to it }
        }
        throw direct.exceptionOrNull() ?: IOException("当前已启用音源均未返回可播放地址")
    }

    private suspend fun resolveUrl(track: Track, bitrate: Int): AudioLink {
        val json = get(
            "types" to "url",
            "id" to track.id,
            "source" to track.source,
            "br" to bitrate.toString(),
        )
        serviceError(json)?.let { throw IOException(it) }
        val value = JsonParser.parseString(json).asJsonObject
        val url = value.string("url")
        if (url.isBlank()) throw IOException("当前音乐源未返回可播放地址")
        return AudioLink(
            url = absoluteUrl(url),
            bitrate = value.int("br") ?: bitrate,
            sizeBytes = value.long("size") ?: 0L,
        )
    }

    suspend fun resolvePicture(track: Track): String? {
        val pictureId = track.pictureId?.takeIf(String::isNotBlank) ?: return null
        val json = get(
            "types" to "pic",
            "id" to pictureId,
            "source" to track.source,
            "size" to "500",
        )
        return JsonParser.parseString(json).asJsonObject.string("url")
            .takeIf(String::isNotBlank)
            ?.let(::absoluteUrl)
    }

    private suspend fun resolveLyric(track: Track): String {
        val lyricId = track.lyricId?.takeIf(String::isNotBlank) ?: return ""
        val json = get(
            "types" to "lyric",
            "id" to lyricId,
            "source" to track.source,
        )
        return JsonParser.parseString(json).asJsonObject.string("lyric")
    }

    private suspend fun get(vararg parameters: Pair<String, String>): String = withContext(Dispatchers.IO) {
        val url = API_URL.toHttpUrl().newBuilder().apply {
            parameters.forEach { (name, value) -> addQueryParameter(name, value) }
        }.build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "GDMusic-Android/${BuildInfo.VERSION}")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val content = response.body.string()
            if (!response.isSuccessful) {
                val message = when (response.code) {
                    429 -> "请求过于频繁，请稍后再试（服务限制为 5 分钟 50 次）"
                    else -> "音乐服务返回 ${response.code}"
                }
                throw IOException(message)
            }
            content
        }
    }

    private fun absoluteUrl(value: String): String = value

    private fun serviceError(json: String): String? = runCatching {
        JsonParser.parseString(json).takeIf { it.isJsonObject }?.asJsonObject?.string("detail")
    }.getOrNull()?.takeIf(String::isNotBlank)

    private fun sourceError(source: MusicSource, detail: String): String = when {
        detail.contains("not supported", ignoreCase = true) -> "${source.displayName} 当前未开放，请在音源管理中调整优先级"
        else -> detail
    }

    private object BuildInfo {
        const val VERSION = "2.2.0"
    }

    private companion object {
        const val API_URL = "https://music-api.gdstudio.xyz/api.php"
    }
}

private fun Track.matches(other: Track, requireArtist: Boolean): Boolean {
    if (name.normalized() != other.name.normalized()) return false
    if (!requireArtist) return true
    val expectedArtists = other.artists.map(String::normalized).toSet()
    return artists.map(String::normalized).any(expectedArtists::contains)
}

private fun String.normalized(): String = lowercase().filter(Char::isLetterOrDigit)

internal object GdMusicJsonParser {
    fun parseTracks(json: String): List<Track> {
        val root = JsonParser.parseString(json)
        if (!root.isJsonArray) return emptyList()
        return root.asJsonArray.mapNotNull(::parseTrack)
    }

    private fun parseTrack(element: com.google.gson.JsonElement): Track? {
        if (!element.isJsonObject) return null
        val value = element.asJsonObject
        val id = value.string("id").takeIf(String::isNotBlank) ?: return null
        val artists = value.get("artist")?.let { artistValue ->
            when {
                artistValue.isJsonArray -> artistValue.asJsonArray.strings()
                artistValue.isJsonPrimitive -> listOf(artistValue.asString)
                else -> emptyList()
            }
        }.orEmpty()
        val extra = value.getAsJsonObject("extra_data")
        return Track(
            id = id,
            name = value.string("name").ifBlank { "未知歌曲" },
            artists = artists.ifEmpty { listOf("未知歌手") },
            album = value.string("album").ifBlank { "未知专辑" },
            source = value.string("source"),
            urlId = value.string("url_id").ifBlank { id },
            pictureId = value.nullableString("pic_id"),
            lyricId = value.nullableString("lyric_id"),
            durationSeconds = extra?.int("duration") ?: 0,
            hasHiRes = extra?.get("has_hires")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: false,
        )
    }
}

private fun JsonObject.string(key: String): String =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString.orEmpty()

private fun JsonObject.nullableString(key: String): String? = string(key).takeIf(String::isNotBlank)

private fun JsonObject.int(key: String): Int? =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asInt

private fun JsonObject.long(key: String): Long? =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asLong

private fun JsonArray.strings(): List<String> = mapNotNull {
    it.takeIf { item -> !item.isJsonNull && item.isJsonPrimitive }?.asString
}
