package com.wlitkopa.thoughts.presentation.screen.thoughts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.wlitkopa.thoughts.domain.model.Category
import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtsUseCase
import org.koin.compose.koinInject

enum class ThoughtSortOrder { DATE, AUTHOR, CATEGORY }

class ThoughtsListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val getAllThoughts: GetAllThoughtsUseCase = koinInject()
        val getAllCategories: GetAllCategoriesUseCase = koinInject()

        val thoughts by getAllThoughts().collectAsState(initial = emptyList())
        val categories by getAllCategories().collectAsState(initial = emptyList())
        val categoryMap = categories.associateBy { it.id }

        var sortOrder by remember { mutableStateOf(ThoughtSortOrder.DATE) }

        val sorted = when (sortOrder) {
            ThoughtSortOrder.DATE -> thoughts.sortedByDescending { it.createdAt }
            ThoughtSortOrder.AUTHOR -> thoughts.sortedBy { it.author.ifBlank { "\uFFFF" } }
            ThoughtSortOrder.CATEGORY -> thoughts.sortedBy { categoryMap[it.categoryId]?.name ?: "\uFFFF" }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("All Thoughts") },
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
            ) {
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

                if (sorted.isEmpty()) {
                    Text(
                        text = "No thoughts yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sorted) { thought ->
                            ThoughtItem(thought = thought, category = categoryMap[thought.categoryId])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThoughtItem(thought: Thought, category: Category?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
    }
}
