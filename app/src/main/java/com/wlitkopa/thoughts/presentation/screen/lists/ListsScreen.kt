package com.wlitkopa.thoughts.presentation.screen.lists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.wlitkopa.thoughts.domain.usecase.list.DeleteSavedListUseCase
import com.wlitkopa.thoughts.domain.usecase.list.GetAllSavedListsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class ListsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getAllSavedLists: GetAllSavedListsUseCase = koinInject()
        val deleteList: DeleteSavedListUseCase = koinInject()
        val scope = rememberCoroutineScope()

        val lists by getAllSavedLists().collectAsState(initial = emptyList())
        var listToDelete by remember { mutableStateOf<SavedList?>(null) }
        var selectedIds by remember { mutableStateOf(emptySet<String>()) }
        var showBulkDeleteDialog by remember { mutableStateOf(false) }

        val isSelecting = selectedIds.isNotEmpty()

        BackHandler(enabled = isSelecting) { selectedIds = emptySet() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (isSelecting) Text("${selectedIds.size} selected")
                        else Text("Lists")
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                if (!isSelecting) {
                    FloatingActionButton(
                        onClick = { navigator.push(AddEditListScreen()) },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add list")
                    }
                }
            },
            bottomBar = {
                if (isSelecting) {
                    BottomAppBar {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { selectedIds = emptySet() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = { showBulkDeleteDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text("Delete (${selectedIds.size})")
                            }
                        }
                    }
                }
            }
        ) { padding ->
            if (lists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No lists yet.\nTap + to create one.",
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
                    items(lists, key = { it.id }) { savedList ->
                        SavedListItem(
                            savedList = savedList,
                            isSelecting = isSelecting,
                            isSelected = selectedIds.contains(savedList.id),
                            onClick = {
                                if (isSelecting) {
                                    selectedIds = if (selectedIds.contains(savedList.id))
                                        selectedIds - savedList.id
                                    else
                                        selectedIds + savedList.id
                                } else {
                                    navigator.push(ListDetailScreen(savedList.id))
                                }
                            },
                            onLongClick = {
                                selectedIds = selectedIds + savedList.id
                            },
                            onEdit = { navigator.push(AddEditListScreen(savedList.id)) },
                            onDelete = { listToDelete = savedList }
                        )
                    }
                }
            }
        }

        listToDelete?.let { list ->
            AlertDialog(
                onDismissRequest = { listToDelete = null },
                title = { Text("Delete list") },
                text = { Text("Delete \"${list.name}\"? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { deleteList(list.id) }
                        listToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listToDelete = null }) { Text("Cancel") }
                }
            )
        }

        if (showBulkDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showBulkDeleteDialog = false },
                title = { Text("Delete lists") },
                text = { Text("Delete ${selectedIds.size} list${if (selectedIds.size == 1) "" else "s"}? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        val toDelete = selectedIds
                        scope.launch { toDelete.forEach { deleteList(it) } }
                        selectedIds = emptySet()
                        showBulkDeleteDialog = false
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBulkDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedListItem(
    savedList: SavedList,
    isSelecting: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelecting) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = savedList.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (savedList.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = savedList.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val summary = buildList {
                    if (savedList.filterCategoryIds.isNotEmpty())
                        add("${savedList.filterCategoryIds.size} categor${if (savedList.filterCategoryIds.size == 1) "y" else "ies"}")
                    if (savedList.filterTags.isNotEmpty())
                        add("${savedList.filterTags.size} tag${if (savedList.filterTags.size == 1) "" else "s"}")
                    if (savedList.pinnedThoughtIds.isNotEmpty())
                        add("${savedList.pinnedThoughtIds.size} pinned")
                }.joinToString(" · ")
                if (summary.isNotEmpty()) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (!isSelecting) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
