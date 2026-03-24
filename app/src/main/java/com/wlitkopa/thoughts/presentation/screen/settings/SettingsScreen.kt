package com.wlitkopa.thoughts.presentation.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.wlitkopa.thoughts.data.local.ThemeRepository
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetRandomThoughtUseCase
import com.wlitkopa.thoughts.notification.CronParser
import com.wlitkopa.thoughts.notification.DEFAULT_CRON
import com.wlitkopa.thoughts.notification.NotificationPreferences
import com.wlitkopa.thoughts.notification.NotificationScheduler
import com.wlitkopa.thoughts.notification.showThoughtNotification
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val cronPresets = listOf(
    "0 8 * * *"    to "Daily at 08:00",
    "0 8,20 * * *" to "Twice a day (8 & 20)",
    "0 */6 * * *"  to "Every 6 hours",
    "0 9 * * 1-5"  to "Weekdays at 09:00",
    "0 10 * * 0,6" to "Weekends at 10:00",
    "0 8 * * 1"    to "Every Monday at 08:00"
)

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val themeRepository: ThemeRepository = koinInject()
        val notifPrefs: NotificationPreferences = koinInject()
        val scheduler: NotificationScheduler = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val getRandomThought: GetRandomThoughtUseCase = koinInject()

        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val isDarkTheme by themeRepository.isDarkTheme.collectAsState()
        val categories by getAllCategories().collectAsState(initial = emptyList())

        var notifEnabled by remember { mutableStateOf(notifPrefs.isEnabled) }
        var cronInput by remember { mutableStateOf(notifPrefs.cronExpression) }
        var selectedCategoryIds by remember { mutableStateOf(notifPrefs.categoryIds) }

        val cronValid by remember { derivedStateOf { CronParser.isValid(cronInput) } }
        val nextExecutions by remember {
            derivedStateOf {
                if (cronValid) CronParser.describeNext(cronInput, 3) else emptyList()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Appearance ───────────────────────────────────────────────
                SectionHeader("Appearance")
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

                // ── Notifications ────────────────────────────────────────────
                SectionHeader("Notifications")
                ListItem(
                    headlineContent = { Text("Enable notifications") },
                    supportingContent = { Text("Receive a random thought on a cron schedule") },
                    trailingContent = {
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = { enabled ->
                                notifEnabled = enabled
                                notifPrefs.isEnabled = enabled
                                if (enabled && cronValid) {
                                    notifPrefs.cronExpression = cronInput
                                    scheduler.schedule(cronInput)
                                    scope.launch {
                                        val next = CronParser.describeNext(cronInput, 1).firstOrNull()
                                        snackbarHostState.showSnackbar("Scheduled. Next: $next")
                                    }
                                } else {
                                    scheduler.cancel()
                                }
                            }
                        )
                    }
                )

                AnimatedVisibility(visible = notifEnabled) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Cron expression input
                        OutlinedTextField(
                            value = cronInput,
                            onValueChange = { cronInput = it },
                            label = { Text("Cron expression") },
                            placeholder = { Text(DEFAULT_CRON) },
                            supportingText = {
                                Text(
                                    "min  hour  day  month  weekday",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            isError = !cronValid,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (!cronValid) {
                            Text(
                                "Invalid cron expression",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Next executions preview
                        if (cronValid && nextExecutions.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Next executions:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                nextExecutions.forEach { label ->
                                    Text(
                                        "  • $label",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Presets
                        Text(
                            "Presets:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            cronPresets.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { (cron, label) ->
                                        SuggestionChip(
                                            onClick = { cronInput = cron },
                                            label = {
                                                Text(
                                                    label,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Categories filter
                        if (categories.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                "Categories (none selected = all)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            categories.forEach { category ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedCategoryIds.contains(category.id),
                                        onCheckedChange = { checked ->
                                            selectedCategoryIds = if (checked)
                                                selectedCategoryIds + category.id
                                            else
                                                selectedCategoryIds - category.id
                                        }
                                    )
                                    Text(category.name, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Apply
                        Button(
                            onClick = {
                                notifPrefs.cronExpression = cronInput
                                notifPrefs.categoryIds = selectedCategoryIds
                                scheduler.schedule(cronInput)
                                scope.launch {
                                    val next = CronParser.describeNext(cronInput, 1).firstOrNull()
                                    snackbarHostState.showSnackbar("Saved. Next notification: $next")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = cronValid
                        ) {
                            Text("Apply")
                        }

                        // Test notification
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val thought = getRandomThought(
                                        categoryIds = selectedCategoryIds.toList()
                                    )
                                    if (thought != null) {
                                        showThoughtNotification(context, thought)
                                        snackbarHostState.showSnackbar("Test notification sent!")
                                    } else {
                                        snackbarHostState.showSnackbar("No thoughts found to show.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send test notification")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
