package com.wlitkopa.thoughts.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.usecase.thought.GetRandomThoughtUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent() {
    val getRandomThought: GetRandomThoughtUseCase = koinInject()
    var thought by remember { mutableStateOf<Thought?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun draw() {
        scope.launch {
            isLoading = true
            thought = getRandomThought(excludeId = thought?.id ?: "")
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { draw() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thoughts") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            } else if (thought != null) {
                ThoughtCard(thought = thought!!)
            } else {
                Text(
                    text = "No thoughts yet.\nAdd some in Categories!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { draw() },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Draw Another")
            }
        }
    }
}

@Composable
private fun ThoughtCard(thought: Thought) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "\u201C${thought.content}\u201D",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (thought.author.isNotBlank() || thought.source.isNotBlank()) {
                val attribution = listOf(thought.author, thought.source)
                    .filter { it.isNotBlank() }
                    .joinToString(" \u2014 ")
                Text(
                    text = "~ $attribution",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
