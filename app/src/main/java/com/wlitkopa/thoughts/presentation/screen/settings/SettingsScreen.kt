package com.wlitkopa.thoughts.presentation.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.wlitkopa.thoughts.data.local.ThemeRepository
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val themeRepository: ThemeRepository = koinInject()
        val isDarkTheme by themeRepository.isDarkTheme.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ListItem(
                    headlineContent = { Text("Dark mode") },
                    supportingContent = { Text("Switch between light and dark theme") },
                    trailingContent = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { themeRepository.setDarkTheme(it) }
                        )
                    }
                )
                HorizontalDivider()
                // Notification settings będą tutaj w kolejnym kroku
            }
        }
    }
}
