package com.gdstudio.music.next.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AE582),
    onPrimary = Color(0xFF00390E),
    primaryContainer = Color(0xFF0B5223),
    onPrimaryContainer = Color(0xFF9BFAA0),
    secondary = Color(0xFFC1C4FF),
    onSecondary = Color(0xFF292B60),
    secondaryContainer = Color(0xFF3F4178),
    onSecondaryContainer = Color(0xFFE1E0FF),
    background = Color(0xFF0F0F23),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF0F0F23),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF27273B),
    onSurfaceVariant = Color(0xFFC9C5D0),
    outline = Color(0xFF92909A),
    error = Color(0xFFFFB4AB),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF116B2D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA4F5A8),
    onPrimaryContainer = Color(0xFF002108),
    secondary = Color(0xFF53558A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E0FF),
    onSecondaryContainer = Color(0xFF0F1244),
    background = Color(0xFFFDF8FD),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFDF8FD),
    onSurface = Color(0xFF1D1B20),
)

@Composable
fun GdMusicTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = when (settings.mode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val seed = when (settings.seedColor) {
        SeedColor.GREEN -> Color(0xFF116B2D)
        SeedColor.PURPLE -> Color(0xFF6750A4)
        SeedColor.BLUE -> Color(0xFF0061A4)
        SeedColor.ORANGE -> Color(0xFF8B5000)
    }
    val seedContainer = when (settings.seedColor) {
        SeedColor.GREEN -> if (darkTheme) Color(0xFF0B5223) else Color(0xFFA4F5A8)
        SeedColor.PURPLE -> if (darkTheme) Color(0xFF4F378B) else Color(0xFFE9DDFF)
        SeedColor.BLUE -> if (darkTheme) Color(0xFF00497D) else Color(0xFFCFE5FF)
        SeedColor.ORANGE -> if (darkTheme) Color(0xFF6A3C00) else Color(0xFFFFDDB8)
    }
    val colors = when {
        settings.monetEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors.copy(
            primary = seed.copy(alpha = 0.9f),
            primaryContainer = seedContainer,
        )
        else -> LightColors.copy(
            primary = seed,
            primaryContainer = seedContainer,
        )
    }
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
