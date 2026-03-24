package com.wlitkopa.thoughts.presentation.screen.thoughts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class ThoughtSortOrder { DATE, AUTHOR, CATEGORY }

class ThoughtsListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getAllThoughts: GetAllThoughtsUseCase = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val searchThoughts: SearchThoughtsUseCase = koinInject()
        val deleteThought: DeleteThoughtUseCase = koinInject()
        val scope = rememberCoroutineScope()

        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var sortOrder by remember { mutableStateOf(ThoughtSortOrder.DATE) }
        var thoughtToDelete by remember { mutableStateOf<Thought?>(null) }

        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(isSearchActive) {
            if (isSearchActive) focusRequester.requestFocus()
        }

        // Switch flow source based on search state
        val thoughts by remember(searchQuery, isSearchActive) {
            if (isSearchActive && searchQuery.isNotBlank())
                searchThoughts(searchQuery)
            else
                getAllThoughts()
        }.collectAsState(initial = emptyList())

        val categories by getAllCategories().collectAsState(initial = emptyList())
        val categoryMap = categories.associateBy { it.id }

        val displayed = if (isSearchActive && searchQuery.isNotBlank()) {
            thoughts // search results — keep repository order
        } else {
            when (sortOrder) {
                ThoughtSortOrder.DATE -> thoughts.sortedByDescending { it.createdAt }
                ThoughtSortOrder.AUTHOR -> thoughts.sortedBy { it.author.ifBlank { "\uFFFF" } }
                ThoughtSortOrder.CATEGORY -> thoughts.sortedBy { categoryMap[it.categoryId]?.name ?: "\uFFFF" }
            }
        }

        Scaffold(
            topBar = {
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(AddEditThoughtScreen()) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add thought")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Sort chips — hidden during search
                if (!isSearchActive) {
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
                                onEdit = { navigator.push(AddEditThoughtScreen(thought.id)) },
                                onDelete = { thoughtToDelete = thought },
                                onClick = { navigator.push(ThoughtDetailScreen(thought.id)) }
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
                    TextButton(onClick = { thoughtToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ThoughtItem(
    thought: Thought,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
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
