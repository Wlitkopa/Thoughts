package com.wlitkopa.thoughts.presentation.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.wlitkopa.thoughts.data.local.LocalBackupService
import com.wlitkopa.thoughts.data.local.ThemeRepository
import com.wlitkopa.thoughts.data.remote.SupabasePreferences
import com.wlitkopa.thoughts.data.remote.SupabaseSyncService
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetRandomThoughtUseCase
import com.wlitkopa.thoughts.notification.CronParser
import com.wlitkopa.thoughts.notification.DEFAULT_CRON
import com.wlitkopa.thoughts.notification.NotificationPreferences
import com.wlitkopa.thoughts.notification.NotificationScheduler
import com.wlitkopa.thoughts.notification.showThoughtNotification
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val cronPresets = listOf(
    "0 8 * * *"    to "Daily at 08:00",
    "0 8,20 * * *" to "Twice a day (8 & 20)",
    "0 */6 * * *"  to "Every 6 hours",
    "0 9 * * 1-5"  to "Weekdays at 09:00",
    "0 10 * * 0,6" to "Weekends at 10:00",
    "0 8 * * 1"    to "Every Monday at 08:00"
)

private val SUPABASE_SETUP_SQL = """
-- Run this in your Supabase SQL Editor

CREATE TABLE IF NOT EXISTS categories (
    id text PRIMARY KEY,
    name text NOT NULL,
    description text NOT NULL DEFAULT '',
    tags text[] NOT NULL DEFAULT '{}',
    include_in_notifications boolean NOT NULL DEFAULT true,
    created_at bigint NOT NULL,
    color text NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS thoughts (
    id text NOT NULL PRIMARY KEY,
    content text NOT NULL,
    author text NOT NULL DEFAULT '',
    source text NOT NULL DEFAULT '',
    note text NOT NULL DEFAULT '',
    category_id text NOT NULL,
    tags text[] NOT NULL DEFAULT '{}',
    include_in_draws boolean NOT NULL DEFAULT true,
    created_at bigint NOT NULL,
    updated_at bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS saved_lists (
    id text NOT NULL PRIMARY KEY,
    name text NOT NULL,
    description text NOT NULL DEFAULT '',
    filter_category_ids text[] NOT NULL DEFAULT '{}',
    filter_tags text[] NOT NULL DEFAULT '{}',
    filter_authors text[] NOT NULL DEFAULT '{}',
    filter_sources text[] NOT NULL DEFAULT '{}',
    pinned_thought_ids text[] NOT NULL DEFAULT '{}',
    created_at bigint NOT NULL
);

-- Disable Row Level Security (for personal use without authentication)
ALTER TABLE categories DISABLE ROW LEVEL SECURITY;
ALTER TABLE thoughts DISABLE ROW LEVEL SECURITY;
ALTER TABLE saved_lists DISABLE ROW LEVEL SECURITY;
""".trimIndent()

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val themeRepository: ThemeRepository = koinInject()
        val notifPrefs: NotificationPreferences = koinInject()
        val scheduler: NotificationScheduler = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val getAllTags: GetAllThoughtTagsUseCase = koinInject()
        val getRandomThought: GetRandomThoughtUseCase = koinInject()
        val supabasePrefs: SupabasePreferences = koinInject()
        val syncService: SupabaseSyncService = koinInject()
        val backupService: LocalBackupService = koinInject()
        val clipboardManager = LocalClipboardManager.current

        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val isDarkTheme by themeRepository.isDarkTheme.collectAsState()
        val categories by getAllCategories().collectAsState(initial = emptyList())
        val allTags by getAllTags().collectAsState(initial = emptyList())

        var notifEnabled by remember { mutableStateOf(notifPrefs.isEnabled) }
        var cronInput by remember { mutableStateOf(notifPrefs.cronExpression) }
        var selectedCategoryIds by remember { mutableStateOf(notifPrefs.categoryIds) }
        var selectedTags by remember { mutableStateOf(notifPrefs.tags) }

        var backupWorking by remember { mutableStateOf(false) }
        var showUploadConfirm by remember { mutableStateOf(false) }
        var showSupabaseImportConfirm by remember { mutableStateOf(false) }

        val exportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                backupWorking = true
                runCatching {
                    val json = backupService.createBackup()
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }.onSuccess {
                    snackbarHostState.showSnackbar("Backup exported successfully!")
                }.onFailure { e ->
                    snackbarHostState.showSnackbar("Export failed: ${e.message}")
                }
                backupWorking = false
            }
        }

        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                backupWorking = true
                runCatching {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        ?: error("Could not read file")
                    backupService.restoreBackup(json)
                }.onSuccess { (cats, thoughts, lists) ->
                    snackbarHostState.showSnackbar("Imported: $cats categories, $thoughts thoughts, $lists lists")
                }.onFailure { e ->
                    snackbarHostState.showSnackbar("Import failed: ${e.message}")
                }
                backupWorking = false
            }
        }

        val cronValid by remember { derivedStateOf { CronParser.isValid(cronInput) } }
        val nextExecutions by remember {
            derivedStateOf {
                if (cronValid) CronParser.describeNext(cronInput, 3) else emptyList()
            }
        }

        // Supabase state
        var supabaseUrl by remember { mutableStateOf(supabasePrefs.url) }
        var supabaseKey by remember { mutableStateOf(supabasePrefs.anonKey) }
        var supabaseKeyVisible by remember { mutableStateOf(false) }
        var supabaseSyncing by remember { mutableStateOf(false) }
        var showSqlSetup by remember { mutableStateOf(false) }

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

                        if (allTags.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                "Tags (none selected = all)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            allTags.forEach { tag ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedTags.contains(tag),
                                        onCheckedChange = { checked ->
                                            selectedTags = if (checked)
                                                selectedTags + tag
                                            else
                                                selectedTags - tag
                                        }
                                    )
                                    Text(tag, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                notifPrefs.cronExpression = cronInput
                                notifPrefs.categoryIds = selectedCategoryIds
                                notifPrefs.tags = selectedTags
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

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val thought = getRandomThought(
                                        categoryIds = selectedCategoryIds.toList(),
                                        tags = selectedTags.toList()
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

                HorizontalDivider()

                // ── Supabase Sync ────────────────────────────────────────────
                SectionHeader("Supabase Sync")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Connect to your own Supabase project to sync and import data across devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = supabaseUrl,
                        onValueChange = { supabaseUrl = it },
                        label = { Text("Supabase URL") },
                        placeholder = { Text("https://xxxx.supabase.co") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = supabaseKey,
                        onValueChange = { supabaseKey = it },
                        label = { Text("Anon Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (supabaseKeyVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { supabaseKeyVisible = !supabaseKeyVisible }) {
                                Icon(
                                    imageVector = if (supabaseKeyVisible) Icons.Default.Visibility
                                                  else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle key visibility"
                                )
                            }
                        }
                    )

                    Button(
                        onClick = {
                            supabasePrefs.url = supabaseUrl.trim()
                            supabasePrefs.anonKey = supabaseKey.trim()
                            scope.launch { snackbarHostState.showSnackbar("Credentials saved.") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()
                    ) {
                        Text("Save credentials")
                    }

                    // Operations row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    supabaseSyncing = true
                                    syncService.testConnection()
                                        .onSuccess {
                                            snackbarHostState.showSnackbar("Connected successfully!")
                                        }
                                        .onFailure { e ->
                                            snackbarHostState.showSnackbar("Failed: ${e.message}")
                                        }
                                    supabaseSyncing = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !supabaseSyncing && supabasePrefs.isConfigured
                        ) {
                            Text("Test")
                        }

                        Button(
                            onClick = { showUploadConfirm = true },
                            modifier = Modifier.weight(1f),
                            enabled = !supabaseSyncing && supabasePrefs.isConfigured
                        ) {
                            Text("Upload")
                        }

                        Button(
                            onClick = { showSupabaseImportConfirm = true },
                            modifier = Modifier.weight(1f),
                            enabled = !supabaseSyncing && supabasePrefs.isConfigured
                        ) {
                            Text("Import")
                        }
                    }

                    if (supabaseSyncing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // SQL setup toggle
                    TextButton(
                        onClick = { showSqlSetup = !showSqlSetup },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showSqlSetup) "Hide setup SQL ▲" else "Show setup SQL ▼")
                    }

                    AnimatedVisibility(visible = showSqlSetup) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Run this SQL in your Supabase project → SQL Editor:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            SelectionContainer {
                                Text(
                                    text = SUPABASE_SETUP_SQL,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(SUPABASE_SETUP_SQL))
                                    scope.launch { snackbarHostState.showSnackbar("SQL copied to clipboard!") }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Copy SQL")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider()

                // ── Local Backup ─────────────────────────────────────────────
                SectionHeader("Local Backup")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Export all your data to a JSON file or restore from a previous backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                exportLauncher.launch("thoughts_backup_$date.json")
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !backupWorking
                        ) {
                            Text("Export")
                        }

                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            enabled = !backupWorking
                        ) {
                            Text("Import")
                        }
                    }

                    if (backupWorking) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showUploadConfirm) {
            AlertDialog(
                onDismissRequest = { showUploadConfirm = false },
                title = { Text("Upload to Supabase") },
                text = { Text("This will overwrite Supabase with your current local data. Records deleted locally will also be removed from Supabase.") },
                confirmButton = {
                    TextButton(onClick = {
                        showUploadConfirm = false
                        scope.launch {
                            supabaseSyncing = true
                            syncService.upload()
                                .onSuccess { snackbarHostState.showSnackbar("Upload complete!") }
                                .onFailure { e -> snackbarHostState.showSnackbar("Upload failed: ${e.message}") }
                            supabaseSyncing = false
                        }
                    }) { Text("Upload") }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadConfirm = false }) { Text("Cancel") }
                }
            )
        }

        if (showSupabaseImportConfirm) {
            AlertDialog(
                onDismissRequest = { showSupabaseImportConfirm = false },
                title = { Text("Import from Supabase") },
                text = { Text("This will merge Supabase data into your local database. Existing local records will be updated, new ones added.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSupabaseImportConfirm = false
                        scope.launch {
                            supabaseSyncing = true
                            syncService.download()
                                .onSuccess { (cats, thoughts, lists) ->
                                    snackbarHostState.showSnackbar("Imported: $cats categories, $thoughts thoughts, $lists lists")
                                }
                                .onFailure { e -> snackbarHostState.showSnackbar("Import failed: ${e.message}") }
                            supabaseSyncing = false
                        }
                    }) { Text("Import") }
                },
                dismissButton = {
                    TextButton(onClick = { showSupabaseImportConfirm = false }) { Text("Cancel") }
                }
            )
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
