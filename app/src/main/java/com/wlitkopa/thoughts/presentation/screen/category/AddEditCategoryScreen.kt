package com.wlitkopa.thoughts.presentation.screen.category

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.wlitkopa.thoughts.domain.model.Category
import com.wlitkopa.thoughts.domain.usecase.category.AddCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetCategoryByIdUseCase
import com.wlitkopa.thoughts.domain.usecase.category.UpdateCategoryUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

class AddEditCategoryScreen(val categoryId: String? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val getCategoryById: GetCategoryByIdUseCase = koinInject()
        val addCategory: AddCategoryUseCase = koinInject()
        val updateCategory: UpdateCategoryUseCase = koinInject()
        val scope = rememberCoroutineScope()

        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var tagsInput by remember { mutableStateOf("") }
        var includeInNotifications by remember { mutableStateOf(true) }
        var existingCategory by remember { mutableStateOf<Category?>(null) }

        LaunchedEffect(categoryId) {
            if (categoryId != null) {
                getCategoryById(categoryId).collect { category ->
                    if (category != null && existingCategory == null) {
                        existingCategory = category
                        name = category.name
                        description = category.description
                        tagsInput = category.tags.joinToString(", ")
                        includeInNotifications = category.includeInNotifications
                    }
                }
            }
        }

        val isEditing = categoryId != null
        val title = if (isEditing) "Edit category" else "Add category"

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
                    Text("Include in notifications", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = includeInNotifications,
                        onCheckedChange = { includeInNotifications = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val tags = tagsInput
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        scope.launch {
                            if (isEditing && existingCategory != null) {
                                updateCategory(
                                    existingCategory!!.copy(
                                        name = name.trim(),
                                        description = description.trim(),
                                        tags = tags,
                                        includeInNotifications = includeInNotifications
                                    )
                                )
                            } else {
                                addCategory(
                                    Category(
                                        id = UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        description = description.trim(),
                                        tags = tags,
                                        includeInNotifications = includeInNotifications
                                    )
                                )
                            }
                            navigator.pop()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank()
                ) {
                    Text(if (isEditing) "Save changes" else "Add category")
                }
            }
        }
    }
}
