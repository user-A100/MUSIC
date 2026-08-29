package com.gdstudio.music.next.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackModeTest {
    @Test
    fun modesCycleInUserVisibleOrder() {
        assertEquals(PlaybackMode.REPEAT_ALL, PlaybackMode.SEQUENTIAL.next())
        assertEquals(PlaybackMode.REPEAT_ONE, PlaybackMode.REPEAT_ALL.next())
        assertEquals(PlaybackMode.SEQUENTIAL, PlaybackMode.REPEAT_ONE.next())
    }
}
