package com.gdstudio.music.next.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.readBytes

class AudioTaggerTest {

    private val audio = byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte()) + ByteArray(512) { it.toByte() }

    @Test
    fun tagMp3_prependsId3v23WithTextAndLyricFrames() {
        val source = tempFile(audio)
        val target = tempFile(ByteArray(0))

        AudioTagger.tagMp3(
            source = source,
            target = target,
            tag = AudioTag(title = "晴天", artist = "周杰伦", album = "叶惠美", lyric = "[00:01.00]故事的小黄花"),
        )

        val output = target.readBytes()
        assertArrayEquals(
            byteArrayOf(0x49, 0x44, 0x33, 0x03, 0x00, 0x80.toByte()),
            output.copyOfRange(0, 6),
        )
        val frames = parseFrames(output)
        assertTrue(frames.containsKey("TIT2"))
        assertTrue(frames.containsKey("TPE1"))
        assertTrue(frames.containsKey("TALB"))
        assertTrue(frames.containsKey("USLT"))
        assertEquals("晴天", frames["TIT2"]!!.decodeText())
        assertArrayEquals(audio, output.copyOfRange(tagSize(output), output.size))
    }

    @Test
    fun tagMp3_dropsExistingId3v2HeaderAndId3v1Trailer() {
        val stale = byteArrayOf(0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40) + ByteArray(64)
        val trailer = "TAG".toByteArray() + ByteArray(125)
        val source = tempFile(stale + audio + trailer)
        val target = tempFile(ByteArray(0))

        AudioTagger.tagMp3(source, target, AudioTag(title = "晴天", artist = "周杰伦", album = "叶惠美"))

        val output = target.readBytes()
        assertArrayEquals(audio, output.copyOfRange(tagSize(output), output.size))
        assertFalse(String(output.copyOfRange(output.size - 128, output.size - 125)) == "TAG")
    }

    @Test
    fun tagMp3_unsynchronisesFramePayload() {
        val cover = byteArrayOf(0x10, 0xFF.toByte(), 0xE0.toByte(), 0x20, 0xFF.toByte(), 0x00, 0x30)
        val source = tempFile(audio)
        val target = tempFile(ByteArray(0))

        AudioTagger.tagMp3(
            source = source,
            target = target,
            tag = AudioTag(title = "cover", artist = "a", album = "b", cover = cover),
        )

        val output = target.readBytes()
        assertTrue(output.copyOfRange(0, tagSize(output)).containsSequence(
            byteArrayOf(0xFF.toByte(), 0x00, 0xE0.toByte()),
        ))
        assertTrue(output.copyOfRange(0, tagSize(output)).containsSequence(
            byteArrayOf(0xFF.toByte(), 0x00, 0x00),
        ))
    }

    @Test
    fun tagMp3_copiesUntaggedAudioWhenThereIsNothingToWrite() {
        val source = tempFile(audio)
        val target = tempFile(ByteArray(0))

        AudioTagger.tagMp3(source, target, AudioTag(title = " ", artist = "", album = ""))

        assertArrayEquals(audio, target.readBytes())
    }

    private fun ByteArray.decodeText(): String {
        assertEquals(0x03.toByte(), this[0])
        return String(this, 1, size - 1, Charsets.UTF_8)
    }

    private fun ByteArray.containsSequence(needle: ByteArray): Boolean {
        outer@ for (index in 0..size - needle.size) {
            for (offset in needle.indices) {
                if (this[index + offset] != needle[offset]) continue@outer
            }
            return true
        }
        return false
    }

    private fun tagSize(bytes: ByteArray): Int = 10 + tagHeaderSize(bytes)

    private fun parseFrames(bytes: ByteArray): Map<String, ByteArray> {
        val end = 10 + tagHeaderSize(bytes)
        val frames = mutableMapOf<String, ByteArray>()
        var offset = 10
        while (offset + 10 <= end) {
            val id = String(bytes, offset, 4, Charsets.ISO_8859_1)
            if (!id.all { it in 'A'..'Z' || it in '0'..'9' }) break
            val size = ((bytes[offset + 4].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 5].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 6].toInt() and 0xFF) shl 8) or
                (bytes[offset + 7].toInt() and 0xFF)
            if (size <= 0 || offset + 10 + size > end) break
            frames[id] = bytes.copyOfRange(offset + 10, offset + 10 + size)
            offset += 10 + size
        }
        return frames
    }

    private fun tagHeaderSize(bytes: ByteArray): Int =
        ((bytes[6].toInt() and 0x7F) shl 21) or
            ((bytes[7].toInt() and 0x7F) shl 14) or
            ((bytes[8].toInt() and 0x7F) shl 7) or
            (bytes[9].toInt() and 0x7F)

    private fun tempFile(content: ByteArray): File =
        File.createTempFile("gdmusic-tag-test", ".mp3").apply {
            deleteOnExit()
            writeBytes(content)
        }
}
