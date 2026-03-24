package com.wlitkopa.thoughts.presentation.screen.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SpeakerNotes
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.wlitkopa.thoughts.presentation.screen.category.CategoryListScreen
import com.wlitkopa.thoughts.presentation.screen.home.HomeContent
import com.wlitkopa.thoughts.presentation.screen.lists.ListsScreen
import com.wlitkopa.thoughts.presentation.screen.settings.SettingsScreen
import com.wlitkopa.thoughts.presentation.screen.thoughts.ThoughtsListScreen

class MainScreen : Screen {

    @Composable
    override fun Content() {
        var selectedTab by remember { mutableIntStateOf(1) } // default: Home
        var previousTab by remember { mutableIntStateOf(1) }
        var dragAccum by remember { mutableFloatStateOf(0f) }
        val swipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }

        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.secondary,
            selectedTextColor = MaterialTheme.colorScheme.secondary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { previousTab = selectedTab; selectedTab = 0 },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Lists", fontSize = 10.sp) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { previousTab = selectedTab; selectedTab = 1 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { previousTab = selectedTab; selectedTab = 2 },
                        icon = { Icon(Icons.AutoMirrored.Filled.SpeakerNotes, contentDescription = null) },
                        label = { Text("Thoughts", fontSize = 10.sp) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { previousTab = selectedTab; selectedTab = 3 },
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("Categories", fontSize = 10.sp) },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { previousTab = selectedTab; selectedTab = 4 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings", fontSize = 10.sp) },
                        colors = navItemColors
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { dragAccum = 0f },
                            onDragCancel = { dragAccum = 0f }
                        ) { _, dragX ->
                            dragAccum += dragX
                            if (dragAccum < -swipeThresholdPx && selectedTab < 4) {
                                previousTab = selectedTab
                                selectedTab++
                                dragAccum = 0f
                            } else if (dragAccum > swipeThresholdPx && selectedTab > 0) {
                                previousTab = selectedTab
                                selectedTab--
                                dragAccum = 0f
                            }
                        }
                    }
            ) {
                val goingRight = selectedTab > previousTab
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (goingRight) 1 else -1
                        (slideInHorizontally(tween(280)) { it * direction } + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(tween(280)) { -it * direction } + fadeOut(tween(180)))
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> Navigator(ListsScreen())
                        1 -> HomeContent()
                        2 -> Navigator(ThoughtsListScreen())
                        3 -> Navigator(CategoryListScreen())
                        else -> Navigator(SettingsScreen())
                    }
                }
            }
        }
    }
}
