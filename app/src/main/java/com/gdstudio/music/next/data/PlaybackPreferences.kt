package com.gdstudio.music.next.data

import android.content.Context

enum class PlaybackMode(val label: String) {
    SEQUENTIAL("顺序播放"),
    REPEAT_ALL("列表循环"),
    REPEAT_ONE("单曲循环"),
}

fun PlaybackMode.next(): PlaybackMode = when (this) {
    PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ALL
    PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
    PlaybackMode.REPEAT_ONE -> PlaybackMode.SEQUENTIAL
}

data class PlaybackOptions(
    val mode: PlaybackMode = PlaybackMode.SEQUENTIAL,
    val shuffleEnabled: Boolean = false,
    val speed: Float = 1f,
)

class PlaybackPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): PlaybackOptions = PlaybackOptions(
        mode = runCatching {
            PlaybackMode.valueOf(preferences.getString(KEY_MODE, null) ?: PlaybackMode.SEQUENTIAL.name)
        }.getOrDefault(PlaybackMode.SEQUENTIAL),
        shuffleEnabled = preferences.getBoolean(KEY_SHUFFLE, false),
        speed = preferences.getFloat(KEY_SPEED, 1f).coerceIn(0.5f, 2f),
    )

    fun save(options: PlaybackOptions) {
        preferences.edit()
            .putString(KEY_MODE, options.mode.name)
            .putBoolean(KEY_SHUFFLE, options.shuffleEnabled)
            .putFloat(KEY_SPEED, options.speed)
            .apply()
    }

    private companion object {
        const val PREFERENCES = "playback_preferences"
        const val KEY_MODE = "playback_mode"
        const val KEY_SHUFFLE = "shuffle_enabled"
        const val KEY_SPEED = "playback_speed"
    }
}
