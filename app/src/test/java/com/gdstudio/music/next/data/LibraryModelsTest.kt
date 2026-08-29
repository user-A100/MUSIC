package com.gdstudio.music.next.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LibraryModelsTest {
    private fun track(source: String, id: String) = Track(
        id = id,
        name = "测试歌曲",
        artists = listOf("测试歌手"),
        album = "测试专辑",
        source = source,
        urlId = id,
        pictureId = null,
        lyricId = null,
        durationSeconds = 180,
        hasHiRes = false,
    )

    @Test
    fun libraryKeySeparatesIdenticalIdsFromDifferentSources() {
        assertNotEquals(track("netease", "42").libraryKey, track("tencent", "42").libraryKey)
    }

    @Test
    fun likedKeysAreDeduplicatedForFastLookup() {
        val item = track("netease", "42")
        assertEquals(setOf("netease:42"), LibraryState(likedTracks = listOf(item, item)).likedKeys)
    }
}
