package com.wlitkopa.thoughts.presentation.screen.lists

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.wlitkopa.thoughts.domain.model.SavedList
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoryTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.list.AddSavedListUseCase
import com.wlitkopa.thoughts.domain.usecase.list.GetSavedListByIdUseCase
import com.wlitkopa.thoughts.domain.usecase.list.UpdateSavedListUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtAuthorsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtSourcesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtTagsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

class AddEditListScreen(val listId: String? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getSavedListById: GetSavedListByIdUseCase = koinInject()
        val addSavedList: AddSavedListUseCase = koinInject()
        val updateSavedList: UpdateSavedListUseCase = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val getAllCategoryTags: GetAllCategoryTagsUseCase = koinInject()
        val getAllThoughtTags: GetAllThoughtTagsUseCase = koinInject()
        val getAllAuthors: GetAllThoughtAuthorsUseCase = koinInject()
        val getAllSources: GetAllThoughtSourcesUseCase = koinInject()
        val scope = rememberCoroutineScope()

        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCategoryIds by remember { mutableStateOf(setOf<String>()) }
        var selectedTags by remember { mutableStateOf(setOf<String>()) }
        var selectedAuthors by remember { mutableStateOf(setOf<String>()) }
        var selectedSources by remember { mutableStateOf(setOf<String>()) }
        var existingList by remember { mutableStateOf<SavedList?>(null) }

        val categories by getAllCategories().collectAsState(initial = emptyList())
        val categoryTags by getAllCategoryTags().collectAsState(initial = emptyList())
        val thoughtTags by getAllThoughtTags().collectAsState(initial = emptyList())
        val allTags = remember(categoryTags, thoughtTags) {
            (categoryTags + thoughtTags).distinct().sorted()
        }
        val authors by getAllAuthors().collectAsState(initial = emptyList())
        val sources by getAllSources().collectAsState(initial = emptyList())

        LaunchedEffect(listId) {
            if (listId != null) {
                getSavedListById(listId).collect { list ->
                    if (list != null && existingList == null) {
                        existingList = list
                        name = list.name
                        description = list.description
                        selectedCategoryIds = list.filterCategoryIds.toSet()
                        selectedTags = list.filterTags.toSet()
                        selectedAuthors = list.filterAuthors.toSet()
                        selectedSources = list.filterSources.toSet()
                    }
                }
            }
        }

        val isEditing = listId != null

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit list" else "New list") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // ── Categories ────────────────────────────────────────────
                HorizontalDivider()
                FilterSection(
                    title = "Filter by categories",
                    subtitle = "Thought included if it belongs to any selected category."
                ) {
                    if (categories.isEmpty()) {
                        EmptyHint("No categories yet.")
                    } else {
                        categories.forEach { category ->
                            CheckRow(
                                label = category.name,
                                checked = category.id in selectedCategoryIds,
                                onCheckedChange = { checked ->
                                    selectedCategoryIds = if (checked)
                                        selectedCategoryIds + category.id
                                    else
                                        selectedCategoryIds - category.id
                                }
                            )
                        }
                    }
                }

                // ── Tags ──────────────────────────────────────────────────
                HorizontalDivider()
                FilterSection(
                    title = "Filter by tags",
                    subtitle = "Thought included if it has any selected tag."
                ) {
                    if (allTags.isEmpty()) {
                        EmptyHint("No tags yet.")
                    } else {
                        allTags.forEach { tag ->
                            CheckRow(
                                label = tag,
                                checked = tag in selectedTags,
                                onCheckedChange = { checked ->
                                    selectedTags = if (checked) selectedTags + tag else selectedTags - tag
                                }
                            )
                        }
                    }
                }

                // ── Authors ───────────────────────────────────────────────
                HorizontalDivider()
                FilterSection(
                    title = "Filter by author",
                    subtitle = "Thought included if its author matches any selected."
                ) {
                    if (authors.isEmpty()) {
                        EmptyHint("No authors yet.")
                    } else {
                        authors.forEach { author ->
                            CheckRow(
                                label = author,
                                checked = author in selectedAuthors,
                                onCheckedChange = { checked ->
                                    selectedAuthors = if (checked)
                                        selectedAuthors + author
                                    else
                                        selectedAuthors - author
                                }
                            )
                        }
                    }
                }

                // ── Sources ───────────────────────────────────────────────
                HorizontalDivider()
                FilterSection(
                    title = "Filter by source",
                    subtitle = "Thought included if its source/book matches any selected."
                ) {
                    if (sources.isEmpty()) {
                        EmptyHint("No sources yet.")
                    } else {
                        sources.forEach { source ->
                            CheckRow(
                                label = source,
                                checked = source in selectedSources,
                                onCheckedChange = { checked ->
                                    selectedSources = if (checked)
                                        selectedSources + source
                                    else
                                        selectedSources - source
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()
                Text(
                    "Individual thoughts can also be pinned from the thought detail screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val pinnedIds = existingList?.pinnedThoughtIds ?: emptyList()
                        scope.launch {
                            if (isEditing && existingList != null) {
                                updateSavedList(
                                    existingList!!.copy(
                                        name = name.trim(),
                                        description = description.trim(),
                                        filterCategoryIds = selectedCategoryIds.toList(),
                                        filterTags = selectedTags.toList(),
                                        filterAuthors = selectedAuthors.toList(),
                                        filterSources = selectedSources.toList(),
                                        pinnedThoughtIds = pinnedIds
                                    )
                                )
                            } else {
                                addSavedList(
                                    SavedList(
                                        id = UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        description = description.trim(),
                                        filterCategoryIds = selectedCategoryIds.toList(),
                                        filterTags = selectedTags.toList(),
                                        filterAuthors = selectedAuthors.toList(),
                                        filterSources = selectedSources.toList()
                                    )
                                )
                            }
                            navigator.pop()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank()
                ) {
                    Text(if (isEditing) "Save changes" else "Create list")
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, subtitle: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    content()
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
