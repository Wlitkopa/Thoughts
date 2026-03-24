package com.wlitkopa.thoughts

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import com.wlitkopa.thoughts.data.local.ThemeRepository
import com.wlitkopa.thoughts.presentation.screen.main.MainScreen
import com.wlitkopa.thoughts.ui.theme.ThoughtsTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val themeRepository: ThemeRepository by inject()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS runtime permission (required on Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeRepository.isDarkTheme.collectAsState()
            ThoughtsTheme(darkTheme = isDarkTheme) {
                Navigator(MainScreen())
            }
        }
    }
}
