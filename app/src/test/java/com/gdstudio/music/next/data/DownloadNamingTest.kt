package com.gdstudio.music.next.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DownloadNamingTest {

    private val track = Track(
        id = "1",
        name = "晴天",
        artists = listOf("周杰伦"),
        album = "叶惠美",
        source = "netease",
        urlId = "1",
        pictureId = "9",
        lyricId = "1",
        durationSeconds = 269,
        hasHiRes = false,
    )

    @Test
    fun fileName_followsWebsiteNameFormats() {
        assertEquals("周杰伦 - 晴天.mp3", downloadFileName(track, NameFormat.ARTIST_TITLE, "mp3"))
        assertEquals("晴天 - 周杰伦.flac", downloadFileName(track, NameFormat.TITLE_ARTIST, "flac"))
        assertEquals("晴天.mp3", downloadFileName(track, NameFormat.TITLE_ONLY, "mp3"))
    }

    @Test
    fun fileName_stripsCharactersTheWebsiteRejects() {
        val dirty = track.copy(name = "A/B:C*D?E\"F<G>H|I=J")
        assertEquals("周杰伦 - A B C D E F G H I J.mp3", downloadFileName(dirty, NameFormat.ARTIST_TITLE, "mp3"))
    }

    @Test
    fun fileName_fallsBackToUntitledAndStaysWithinByteLimit() {
        val blank = track.copy(name = "///", artists = listOf(""))
        assertEquals("untitled.mp3", downloadFileName(blank, NameFormat.TITLE_ONLY, "mp3"))

        val long = track.copy(name = "长".repeat(200), artists = listOf("周杰伦"))
        val name = downloadFileName(long, NameFormat.ARTIST_TITLE, "mp3")
        assertFalse(name.toByteArray().size > 255)
    }

    @Test
    fun extension_usesUrlSuffixWhenWhitelisted() {
        assertEquals("flac", audioExtension("https://host/a/b.flac?token=1", 320))
        assertEquals("m4a", audioExtension("https://host/a/b.m4a", 128))
        assertEquals("mp3", audioExtension("https://host/stream?id=9", 320))
        assertEquals("flac", audioExtension("https://host/stream?id=9", 999))
        assertEquals("mp4", audioExtension("https://host/a/b.mp4?x=1", 128))
    }
}
