package com.gdstudio.music.next

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gdstudio.music.next.download.DownloadPermissions
import com.gdstudio.music.next.ui.GdMusicApp
import com.gdstudio.music.next.ui.theme.GdMusicTheme
import com.gdstudio.music.next.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestDownloadPermissions()
        setContent {
            val themePreferences = remember { ThemePreferences(this) }
            var themeSettings by remember { mutableStateOf(themePreferences.load()) }
            GdMusicTheme(settings = themeSettings) {
                val musicViewModel: MainViewModel = viewModel()
                GdMusicApp(
                    viewModel = musicViewModel,
                    themeSettings = themeSettings,
                    onThemeSettingsChange = { themeSettings = it; themePreferences.save(it) },
                )
            }
        }
    }

    /** Downloads report progress through a notification, so the permission is requested up front. */
    private fun requestDownloadPermissions() {
        val missing = DownloadPermissions.missing(this)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
