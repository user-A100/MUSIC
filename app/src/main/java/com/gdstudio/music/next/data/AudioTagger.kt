package com.gdstudio.music.next.data

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

data class AudioTag(
    val title: String,
    val artist: String,
    val album: String,
    val cover: ByteArray? = null,
    val coverMime: String = "image/jpeg",
    val lyric: String = "",
)

/**
 * Minimal ID3v2.3 writer for the "new download" mode of the website.
 *
 * The site tags MP3 files with title, artist, album, embedded cover (APIC) and unsynchronised
 * lyric (USLT) before saving. Only the frames we need are emitted, an existing ID3v2 header is
 * dropped so tags are never duplicated, and a trailing ID3v1 block is removed so players do not
 * read stale values. Non-MP3 containers are copied through untouched, matching the site which
 * skips metadata for anything but MP3.
 */
object AudioTagger {
    private const val HEADER_SIZE = 10
    private const val ID3V1_SIZE = 128
    private val ID3_MAGIC = byteArrayOf(0x49, 0x44, 0x33) // "ID3"
    private val TAG_MAGIC = byteArrayOf(0x54, 0x41, 0x47) // "TAG"

    /** Writes [source] to [target], prepending a fresh ID3v2.3 tag when there is anything to write. */
    fun tagMp3(source: File, target: File, tag: AudioTag) {
        val frames = buildFrames(tag)
        val merged = frames.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
        val body = if (merged.isEmpty()) merged else unsynchronise(merged)
        val range = audioRange(source)
        target.outputStream().use { output ->
            if (body.isNotEmpty()) {
                output.write(ID3_MAGIC)
                output.write(0x03) // major version 2.3
                output.write(0x00) // revision
                output.write(0x80) // unsynchronisation
                output.write(syncSafe(body.size))
                output.write(body)
            }
            RandomAccessFile(source, "r").use { input ->
                input.seek(range.first)
                val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
                // LongRange bounds are inclusive, so the byte count is last - first + 1.
                var remaining = if (range.isEmpty()) 0L else range.last - range.first + 1L
                while (remaining > 0) {
                    val read = input.read(chunk, 0, minOf(remaining, chunk.size.toLong()).toInt())
                    if (read < 0) break
                    output.write(chunk, 0, read)
                    remaining -= read
                }
            }
        }
    }

    private fun buildFrames(tag: AudioTag): List<ByteArray> = buildList {
        textFrame("TIT2", tag.title)?.let(::add)
        textFrame("TPE1", tag.artist)?.let(::add)
        textFrame("TALB", tag.album)?.let(::add)
        tag.cover?.takeIf { it.isNotEmpty() }?.let { add(coverFrame(it, tag.coverMime)) }
        lyricFrame(tag.lyric)?.let(::add)
    }

    /** Text frames are stored as UTF-8 (encoding byte 0x03) so CJK titles survive. */
    private fun textFrame(id: String, value: String): ByteArray? {
        val text = value.trim()
        if (text.isEmpty()) return null
        return frame(id, byteArrayOf(0x03) + text.toByteArray(StandardCharsets.UTF_8))
    }

    private fun coverFrame(bytes: ByteArray, mime: String): ByteArray {
        val payload = ByteArrayOutputStream()
        payload.write(0x00) // latin-1 mime and description
        payload.write(mime.toByteArray(StandardCharsets.ISO_8859_1))
        payload.write(0x00)
        payload.write(0x03) // picture type: front cover
        payload.write(0x00) // empty description
        payload.write(bytes)
        return frame("APIC", payload.toByteArray())
    }

    private fun lyricFrame(lyric: String): ByteArray? {
        val text = lyric.trim()
        if (text.isEmpty()) return null
        val payload = ByteArrayOutputStream()
        payload.write(0x03) // UTF-8
        payload.write("und".toByteArray(StandardCharsets.ISO_8859_1))
        payload.write(0x00) // empty content descriptor
        payload.write(text.toByteArray(StandardCharsets.UTF_8))
        return frame("USLT", payload.toByteArray())
    }

    private fun frame(id: String, payload: ByteArray): ByteArray =
        id.toByteArray(StandardCharsets.ISO_8859_1) +
            intBytes(payload.size) +
            byteArrayOf(0x00, 0x00) +
            payload

    /**
     * ID3v2.3 requires `0xFF 0x00` and `0xFF 0xE0..0xFF` sequences to be escaped so decoders do
     * not mistake audio payload for a frame sync.
     */
    private fun unsynchronise(data: ByteArray): ByteArray {
        val output = ByteArrayOutputStream(data.size + data.size / 8 + 16)
        for (index in data.indices) {
            val byte = data[index]
            output.write(byte.toInt())
            if (byte != 0xFF.toByte()) continue
            val next = data.getOrNull(index + 1)?.toInt()?.and(0xFF) ?: continue
            if (next == 0x00 || next >= 0xE0) output.write(0x00)
        }
        return output.toByteArray()
    }

    /** Locates the raw MPEG frames, skipping an existing ID3v2 header and trailing ID3v1 block. */
    private fun audioRange(file: File): LongRange {
        var start = 0L
        if (file.length() >= HEADER_SIZE) {
            RandomAccessFile(file, "r").use { input ->
                val header = ByteArray(HEADER_SIZE)
                if (input.read(header) == HEADER_SIZE && header.starts(ID3_MAGIC)) {
                    start = HEADER_SIZE + syncSafeSize(header)
                }
            }
        }
        var end = file.length()
        if (end > ID3V1_SIZE) {
            RandomAccessFile(file, "r").use { input ->
                input.seek(end - ID3V1_SIZE)
                val trailer = ByteArray(TAG_MAGIC.size)
                if (input.read(trailer) == trailer.size && trailer.starts(TAG_MAGIC)) end -= ID3V1_SIZE
            }
        }
        return start.coerceAtMost(end) until end
    }

    private fun syncSafeSize(header: ByteArray): Long =
        ((header[6].toLong() and 0x7F) shl 21) or
            ((header[7].toLong() and 0x7F) shl 14) or
            ((header[8].toLong() and 0x7F) shl 7) or
            (header[9].toLong() and 0x7F)

    private fun syncSafe(size: Int): ByteArray = byteArrayOf(
        ((size shr 21) and 0x7F).toByte(),
        ((size shr 14) and 0x7F).toByte(),
        ((size shr 7) and 0x7F).toByte(),
        (size and 0x7F).toByte(),
    )

    private fun intBytes(value: Int): ByteArray = byteArrayOf(
        (value shr 24).toByte(),
        (value shr 16).toByte(),
        (value shr 8).toByte(),
        value.toByte(),
    )

    private fun ByteArray.starts(magic: ByteArray): Boolean =
        size >= magic.size && magic.indices.all { this[it] == magic[it] }
}
