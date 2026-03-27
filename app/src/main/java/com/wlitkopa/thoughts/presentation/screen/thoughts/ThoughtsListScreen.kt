package com.wlitkopa.thoughts.presentation.screen.thoughts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.wlitkopa.thoughts.domain.model.Category
import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.DeleteThoughtUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.SearchThoughtsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.UpdateThoughtUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class ThoughtSortOrder { DATE, AUTHOR, CATEGORY }

class ThoughtsListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getAllThoughts: GetAllThoughtsUseCase = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val searchThoughts: SearchThoughtsUseCase = koinInject()
        val deleteThought: DeleteThoughtUseCase = koinInject()
        val updateThought: UpdateThoughtUseCase = koinInject()
        val scope = rememberCoroutineScope()

        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var sortOrder by remember { mutableStateOf(ThoughtSortOrder.DATE) }
        var thoughtToDelete by remember { mutableStateOf<Thought?>(null) }
        var selectedIds by remember { mutableStateOf(setOf<String>()) }
        val isMultiSelect = selectedIds.isNotEmpty()
        var showAddTagDialog by remember { mutableStateOf(false) }
        var showRemoveTagDialog by remember { mutableStateOf(false) }
        var showBulkDeleteDialog by remember { mutableStateOf(false) }
        var newTagText by remember { mutableStateOf("") }
        var tagToRemove by remember { mutableStateOf<String?>(null) }

        val focusRequester = remember { FocusRequester() }

        BackHandler(enabled = isMultiSelect) { selectedIds = emptySet() }

        LaunchedEffect(isSearchActive) {
            if (isSearchActive) focusRequester.requestFocus()
        }

        val thoughts by remember(searchQuery, isSearchActive) {
            if (isSearchActive && searchQuery.isNotBlank())
                searchThoughts(searchQuery)
            else
                getAllThoughts()
        }.collectAsState(initial = emptyList())

        val categories by getAllCategories().collectAsState(initial = emptyList())
        val categoryMap = categories.associateBy { it.id }

        val displayed = if (isSearchActive && searchQuery.isNotBlank()) {
            thoughts
        } else {
            when (sortOrder) {
                ThoughtSortOrder.DATE -> thoughts.sortedByDescending { it.createdAt }
                ThoughtSortOrder.AUTHOR -> thoughts.sortedBy { it.author.ifBlank { "\uFFFF" } }
                ThoughtSortOrder.CATEGORY -> thoughts.sortedBy { categoryMap[it.categoryId]?.name ?: "\uFFFF" }
            }
        }

        val tagsInSelection = displayed
            .filter { it.id in selectedIds }
            .flatMap { it.tags }
            .distinct()
            .sorted()

        val topBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Scaffold(
            topBar = {
                if (isMultiSelect) {
                    TopAppBar(
                        title = { Text("${selectedIds.size} selected") },
                        navigationIcon = {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                            }
                        },
                        colors = topBarColors
                    )
                } else {
                    TopAppBar(
                        title = {
                            if (isSearchActive) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search thoughts…") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {}),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                )
                            } else {
                                Text("All Thoughts")
                            }
                        },
                        navigationIcon = {
                            if (isSearchActive) {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Close search")
                                }
                            }
                        },
                        actions = {
                            if (!isSearchActive) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            } else if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = topBarColors
                    )
                }
            },
            bottomBar = {
                if (isMultiSelect) {
                    BottomAppBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        TextButton(onClick = { showAddTagDialog = true }) {
                            Text("Add tag")
                        }
                        TextButton(
                            onClick = { showRemoveTagDialog = true },
                            enabled = tagsInSelection.isNotEmpty()
                        ) {
                            Text("Remove tag")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            val selected = displayed.filter { it.id in selectedIds }
                            selectedIds = emptySet()
                            scope.launch {
                                val allIncluded = selected.all { it.includeInDraws }
                                selected.forEach { updateThought(it.copy(includeInDraws = !allIncluded)) }
                            }
                        }) {
                            Icon(Icons.Default.Casino, contentDescription = "Toggle draws")
                        }
                        IconButton(onClick = { showBulkDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (!isMultiSelect) {
                    FloatingActionButton(
                        onClick = { navigator.push(AddEditThoughtScreen()) },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add thought")
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (!isSearchActive && !isMultiSelect) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortOrder == ThoughtSortOrder.DATE,
                            onClick = { sortOrder = ThoughtSortOrder.DATE },
                            label = { Text("Date") }
                        )
                        FilterChip(
                            selected = sortOrder == ThoughtSortOrder.AUTHOR,
                            onClick = { sortOrder = ThoughtSortOrder.AUTHOR },
                            label = { Text("Author") }
                        )
                        FilterChip(
                            selected = sortOrder == ThoughtSortOrder.CATEGORY,
                            onClick = { sortOrder = ThoughtSortOrder.CATEGORY },
                            label = { Text("Category") }
                        )
                    }
                }

                if (displayed.isEmpty()) {
                    Text(
                        text = if (isSearchActive && searchQuery.isNotBlank())
                            "No results for \"$searchQuery\"."
                        else
                            "No thoughts yet.\nTap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayed, key = { it.id }) { thought ->
                            ThoughtItem(
                                thought = thought,
                                category = categoryMap[thought.categoryId],
                                isSelected = thought.id in selectedIds,
                                isMultiSelectMode = isMultiSelect,
                                onLongClick = { selectedIds = selectedIds + thought.id },
                                onSelect = {
                                    selectedIds = if (thought.id in selectedIds)
                                        selectedIds - thought.id
                                    else
                                        selectedIds + thought.id
                                },
                                onEdit = { navigator.push(AddEditThoughtScreen(thought.id)) },
                                onDelete = { thoughtToDelete = thought },
                                onClick = { navigator.push(ThoughtDetailScreen(thought.id)) },
                                onToggleDraw = {
                                    scope.launch {
                                        updateThought(thought.copy(includeInDraws = !thought.includeInDraws))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        thoughtToDelete?.let { thought ->
            AlertDialog(
                onDismissRequest = { thoughtToDelete = null },
                title = { Text("Delete thought") },
                text = { Text("Delete this thought? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { deleteThought(thought.id) }
                        thoughtToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { thoughtToDelete = null }) { Text("Cancel") }
                }
            )
        }

        if (showBulkDeleteDialog) {
            val count = selectedIds.size
            AlertDialog(
                onDismissRequest = { showBulkDeleteDialog = false },
                title = { Text("Delete $count thought${if (count != 1) "s" else ""}") },
                text = { Text("Delete $count selected thought${if (count != 1) "s" else ""}? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        val ids = selectedIds.toList()
                        showBulkDeleteDialog = false
                        selectedIds = emptySet()
                        scope.launch { ids.forEach { deleteThought(it) } }
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBulkDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showAddTagDialog) {
            AlertDialog(
                onDismissRequest = { showAddTagDialog = false; newTagText = "" },
                title = { Text("Add tag") },
                text = {
                    TextField(
                        value = newTagText,
                        onValueChange = { newTagText = it },
                        placeholder = { Text("Tag name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val tag = newTagText.trim()
                            if (tag.isNotEmpty()) {
                                val selected = displayed.filter { it.id in selectedIds }
                                scope.launch {
                                    selected.forEach { thought ->
                                        if (tag !in thought.tags) {
                                            updateThought(thought.copy(tags = thought.tags + tag))
                                        }
                                    }
                                }
                            }
                            showAddTagDialog = false
                            newTagText = ""
                            selectedIds = emptySet()
                        },
                        enabled = newTagText.isNotBlank()
                    ) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTagDialog = false; newTagText = "" }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showRemoveTagDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveTagDialog = false; tagToRemove = null },
                title = { Text("Remove tag") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(tagsInSelection) { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tagToRemove = tag }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tagToRemove == tag,
                                    onClick = { tagToRemove = tag }
                                )
                                Text(tag, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val tag = tagToRemove ?: return@TextButton
                            val selected = displayed.filter { it.id in selectedIds }
                            scope.launch {
                                selected.forEach { thought ->
                                    if (tag in thought.tags) {
                                        updateThought(thought.copy(tags = thought.tags - tag))
                                    }
                                }
                            }
                            showRemoveTagDialog = false
                            tagToRemove = null
                            selectedIds = emptySet()
                        },
                        enabled = tagToRemove != null
                    ) { Text("Remove") }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveTagDialog = false; tagToRemove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThoughtItem(
    thought: Thought,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
    onToggleDraw: () -> Unit = {},
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onLongClick: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    val dotColor = category?.color?.takeIf { it.isNotEmpty() }?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isMultiSelectMode) onSelect() else onClick() },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() },
                    modifier = Modifier.padding(end = 4.dp)
                )
            } else {
                if (dotColor != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .align(Alignment.Top)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "\u201C${thought.content}\u201D",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val attribution = listOf(thought.author, thought.source)
                    .filter { it.isNotBlank() }
                    .joinToString(" \u2014 ")
                if (attribution.isNotBlank()) {
                    Text(
                        text = "~ $attribution",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (category != null) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!isMultiSelectMode) {
                IconButton(onClick = onToggleDraw, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = if (thought.includeInDraws) "Included in draws" else "Excluded from draws",
                        tint = if (thought.includeInDraws)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
