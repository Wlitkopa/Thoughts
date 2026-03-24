package com.wlitkopa.thoughts.presentation.screen.thoughts

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.AddThoughtUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetThoughtByIdUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.UpdateThoughtUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

class AddEditThoughtScreen(val thoughtId: String? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getThoughtById: GetThoughtByIdUseCase = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()
        val addThought: AddThoughtUseCase = koinInject()
        val updateThought: UpdateThoughtUseCase = koinInject()
        val scope = rememberCoroutineScope()

        val categories by getAllCategories().collectAsState(initial = emptyList())

        // Form state
        var content by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var selectedCategoryId by remember { mutableStateOf("") }
        var tagsInput by remember { mutableStateOf("") }
        var includeInDraws by remember { mutableStateOf(true) }
        var existingThought by remember { mutableStateOf<Thought?>(null) }

        // Load existing thought when editing
        LaunchedEffect(thoughtId) {
            if (thoughtId != null) {
                getThoughtById(thoughtId).collect { thought ->
                    if (thought != null && existingThought == null) {
                        existingThought = thought
                        content = thought.content
                        author = thought.author
                        source = thought.source
                        note = thought.note
                        selectedCategoryId = thought.categoryId
                        tagsInput = thought.tags.joinToString(", ")
                        includeInDraws = thought.includeInDraws
                    }
                }
            }
        }

        // Auto-select first category for new thought
        LaunchedEffect(categories) {
            if (thoughtId == null && selectedCategoryId.isEmpty() && categories.isNotEmpty()) {
                selectedCategoryId = categories.first().id
            }
        }

        val isEditing = thoughtId != null
        val title = if (isEditing) "Edit thought" else "Add thought"
        val contentIsBlank = content.isBlank()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
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
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = contentIsBlank && content.isEmpty()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source (book, article…)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Personal note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Category dropdown
                if (categories.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedCategory = categories.find { it.id == selectedCategoryId }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No categories yet — add one in the Categories tab first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma-separated)") },
                    placeholder = { Text("e.g. theology, family") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Include in random draws", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = includeInDraws, onCheckedChange = { includeInDraws = it })
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (content.isBlank() || selectedCategoryId.isEmpty()) return@Button
                        val tags = tagsInput
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        scope.launch {
                            if (isEditing && existingThought != null) {
                                updateThought(
                                    existingThought!!.copy(
                                        content = content.trim(),
                                        author = author.trim(),
                                        source = source.trim(),
                                        note = note.trim(),
                                        categoryId = selectedCategoryId,
                                        tags = tags,
                                        includeInDraws = includeInDraws,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                addThought(
                                    Thought(
                                        id = UUID.randomUUID().toString(),
                                        content = content.trim(),
                                        author = author.trim(),
                                        source = source.trim(),
                                        note = note.trim(),
                                        categoryId = selectedCategoryId,
                                        tags = tags,
                                        includeInDraws = includeInDraws
                                    )
                                )
                            }
                            navigator.pop()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank() && selectedCategoryId.isNotEmpty()
                ) {
                    Text(if (isEditing) "Save changes" else "Add thought")
                }
            }
        }
    }
}
