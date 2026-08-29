package com.gdstudio.music.next.ui.theme

import android.content.Context

enum class AppThemeMode(val label: String) { SYSTEM("跟随系统"), LIGHT("浅色"), DARK("深色") }
enum class SeedColor(val label: String) { GREEN("清新绿"), PURPLE("律动紫"), BLUE("深海蓝"), ORANGE("暖橙") }

data class ThemeSettings(
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
    val monetEnabled: Boolean = true,
    val seedColor: SeedColor = SeedColor.GREEN,
)

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
    fun load() = ThemeSettings(
        mode = runCatching { AppThemeMode.valueOf(prefs.getString("mode", null) ?: "SYSTEM") }.getOrDefault(AppThemeMode.SYSTEM),
        monetEnabled = prefs.getBoolean("monet", true),
        seedColor = runCatching { SeedColor.valueOf(prefs.getString("seed", null) ?: "GREEN") }.getOrDefault(SeedColor.GREEN),
    )
    fun save(value: ThemeSettings) = prefs.edit().putString("mode", value.mode.name)
        .putBoolean("monet", value.monetEnabled).putString("seed", value.seedColor.name).apply()
}
