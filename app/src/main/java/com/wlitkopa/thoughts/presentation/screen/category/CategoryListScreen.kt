package com.wlitkopa.thoughts.presentation.screen.category

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.wlitkopa.thoughts.domain.model.Category
import com.wlitkopa.thoughts.domain.usecase.category.DeleteCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.category.SearchCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.category.UpdateCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class CategoryListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val searchCategories: SearchCategoriesUseCase = koinInject()
        val getAllThoughts: GetAllThoughtsUseCase = koinInject()
        val deleteCategory: DeleteCategoryUseCase = koinInject()
        val updateCategory: UpdateCategoryUseCase = koinInject()
        val scope = rememberCoroutineScope()

        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var categoryToDelete by remember { mutableStateOf<Category?>(null) }
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

        val categories by remember(searchQuery, isSearchActive) {
            if (isSearchActive && searchQuery.isNotBlank())
                searchCategories(searchQuery)
            else
                getAllCategories()
        }.collectAsState(initial = emptyList())

        val thoughts by getAllThoughts().collectAsState(initial = emptyList())
        val thoughtCountByCategory = thoughts.groupingBy { it.categoryId }.eachCount()

        val tagsInSelection = categories
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
                                    placeholder = { Text("Search categories…") },
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
                                Text("Categories")
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
                            val selected = categories.filter { it.id in selectedIds }
                            selectedIds = emptySet()
                            scope.launch {
                                val allIncluded = selected.all { it.includeInNotifications }
                                selected.forEach {
                                    updateCategory(it.copy(includeInNotifications = !allIncluded))
                                }
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
                        onClick = { navigator.push(AddEditCategoryScreen()) },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add category")
                    }
                }
            }
        ) { padding ->
            if (categories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isSearchActive && searchQuery.isNotBlank())
                            "No results for \"$searchQuery\"."
                        else
                            "No categories yet.\nTap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            thoughtCount = thoughtCountByCategory[category.id] ?: 0,
                            isSelected = category.id in selectedIds,
                            isMultiSelectMode = isMultiSelect,
                            onLongClick = { selectedIds = selectedIds + category.id },
                            onSelect = {
                                selectedIds = if (category.id in selectedIds)
                                    selectedIds - category.id
                                else
                                    selectedIds + category.id
                            },
                            onEdit = { navigator.push(AddEditCategoryScreen(category.id)) },
                            onDelete = { categoryToDelete = category },
                            onToggleDraw = {
                                scope.launch {
                                    updateCategory(category.copy(includeInNotifications = !category.includeInNotifications))
                                }
                            }
                        )
                    }
                }
            }
        }

        categoryToDelete?.let { category ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Delete category") },
                text = { Text("Delete \"${category.name}\"? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { deleteCategory(category.id) }
                        categoryToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) { Text("Cancel") }
                }
            )
        }

        if (showBulkDeleteDialog) {
            val count = selectedIds.size
            AlertDialog(
                onDismissRequest = { showBulkDeleteDialog = false },
                title = { Text("Delete $count categor${if (count != 1) "ies" else "y"}") },
                text = { Text("Delete $count selected categor${if (count != 1) "ies" else "y"}? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        val ids = selectedIds.toList()
                        showBulkDeleteDialog = false
                        selectedIds = emptySet()
                        scope.launch { ids.forEach { deleteCategory(it) } }
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
                                val selected = categories.filter { it.id in selectedIds }
                                scope.launch {
                                    selected.forEach { category ->
                                        if (tag !in category.tags) {
                                            updateCategory(category.copy(tags = category.tags + tag))
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
                            val selected = categories.filter { it.id in selectedIds }
                            scope.launch {
                                selected.forEach { category ->
                                    if (tag in category.tags) {
                                        updateCategory(category.copy(tags = category.tags - tag))
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
private fun CategoryItem(
    category: Category,
    thoughtCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleDraw: () -> Unit = {},
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onLongClick: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    val barColor = if (category.color.isNotEmpty()) {
        runCatching { Color(android.graphics.Color.parseColor(category.color)) }.getOrNull()
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isMultiSelectMode) onSelect() else {} },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() },
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                if (barColor != null) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(72.dp)
                            .background(barColor)
                    )
                } else {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (category.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$thoughtCount thought${if (thoughtCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (!isMultiSelectMode) {
                    IconButton(onClick = onToggleDraw, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = if (category.includeInNotifications) "Included in draws" else "Excluded from draws",
                            tint = if (category.includeInNotifications)
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
}
