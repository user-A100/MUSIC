package com.gdstudio.music.next.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {
    @Test
    fun parseLrc_ordersLinesAndHandlesFractions() {
        val result = parseLrc(
            """
            [00:12.50]第二句
            [00:01.005]第一句
            [00:12.50][00:20.1]重复句
            [ar:artist]
            """.trimIndent(),
        )

        assertEquals(listOf(1_005L, 12_500L, 12_500L, 20_100L), result.map { it.timestampMs })
        assertEquals("第一句", result.first().text)
        assertEquals("重复句", result.last().text)
    }

    @Test
    fun parser_mapsWebsiteSearchShape() {
        val tracks = GdMusicJsonParser.parseTracks(
            """
            [{
              "id":"5257138",
              "name":"屋顶",
              "artist":["周杰伦","温岚"],
              "album":"男女情歌对唱冠军全记录",
              "pic_id":"109951165671182684",
              "url_id":"5257138",
              "lyric_id":"5257138",
              "source":"netease",
              "extra_data":{"duration":319,"has_hires":false}
            }]
            """.trimIndent(),
        )

        assertEquals(1, tracks.size)
        assertEquals("屋顶", tracks.single().name)
        assertEquals("周杰伦, 温岚", tracks.single().artistText)
        assertEquals(319, tracks.single().durationSeconds)
        assertFalse(tracks.single().hasHiRes)
    }

    @Test
    fun parser_skipsEntriesWithoutIdsAndAcceptsStringArtist() {
        val tracks = GdMusicJsonParser.parseTracks(
            """[{"name":"bad"},{"id":"1","name":"ok","artist":"artist","source":"qobuz","extra_data":{"has_hires":true}}]""",
        )

        assertEquals(1, tracks.size)
        assertEquals(listOf("artist"), tracks.single().artists)
        assertTrue(tracks.single().hasHiRes)
    }
}
