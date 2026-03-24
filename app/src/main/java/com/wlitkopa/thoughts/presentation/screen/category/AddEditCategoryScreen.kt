package com.wlitkopa.thoughts.presentation.screen.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// "" = no color (None option)
private val COLOR_PRESETS = listOf(
    "" to "None",
    "#795548" to "Coffee",
    "#BE8C5A" to "Caramel",
    "#C49A6C" to "Sand",
    "#8BC34A" to "Lime",
    "#558B2F" to "Forest",
    "#FF7043" to "Amber",
    "#E53935" to "Red",
    "#1565C0" to "Navy",
    "#29B6F6" to "Sky",
    "#9C27B0" to "Violet",
    "#7B1FA2" to "Plum",
    "#6D4C41" to "Mahogany",
    "#455A64" to "Slate",
)

private fun hexToRgb(hex: String): Triple<Float, Float, Float>? {
    val parsed = runCatching { android.graphics.Color.parseColor(hex) }.getOrNull() ?: return null
    return Triple(
        android.graphics.Color.red(parsed).toFloat(),
        android.graphics.Color.green(parsed).toFloat(),
        android.graphics.Color.blue(parsed).toFloat()
    )
}

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

        // Color state: selectedPreset = "" means custom sliders, "none" means no color, hex string = preset
        var selectedPreset by remember { mutableStateOf("none") }
        var sliderR by remember { mutableFloatStateOf(127f) }
        var sliderG by remember { mutableFloatStateOf(127f) }
        var sliderB by remember { mutableFloatStateOf(127f) }

        val customHex = "#%02X%02X%02X".format(sliderR.toInt(), sliderG.toInt(), sliderB.toInt())
        val effectiveColor: String = when {
            selectedPreset == "none" -> ""
            selectedPreset.isNotEmpty() -> selectedPreset
            else -> customHex
        }
        val previewColor: Color? = if (effectiveColor.isNotEmpty())
            runCatching { Color(android.graphics.Color.parseColor(effectiveColor)) }.getOrNull()
        else null

        LaunchedEffect(categoryId) {
            if (categoryId != null) {
                getCategoryById(categoryId).collect { category ->
                    if (category != null && existingCategory == null) {
                        existingCategory = category
                        name = category.name
                        description = category.description
                        tagsInput = category.tags.joinToString(", ")
                        includeInNotifications = category.includeInNotifications
                        val catColor = category.color
                        when {
                            catColor.isEmpty() -> selectedPreset = "none"
                            COLOR_PRESETS.any { it.first == catColor } -> {
                                selectedPreset = catColor
                                hexToRgb(catColor)?.let { (r, g, b) ->
                                    sliderR = r; sliderG = g; sliderB = b
                                }
                            }
                            else -> {
                                selectedPreset = ""
                                hexToRgb(catColor)?.let { (r, g, b) ->
                                    sliderR = r; sliderG = g; sliderB = b
                                }
                            }
                        }
                    }
                }
            }
        }

        val isEditing = categoryId != null

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit category" else "Add category") },
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

                // ── Color picker ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Color", style = MaterialTheme.typography.bodyMedium)
                    if (previewColor != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(previewColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                        )
                    }
                }

                // Preset circles — two rows
                COLOR_PRESETS.chunked(7).forEach { rowPresets ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowPresets.forEach { (hex, label) ->
                            val isSelected = selectedPreset == hex ||
                                    (hex == "" && selectedPreset == "none")
                            val circleColor = if (hex.isEmpty()) Color.Transparent
                            else runCatching {
                                Color(android.graphics.Color.parseColor(hex))
                            }.getOrDefault(Color.Gray)

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(circleColor)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (hex.isEmpty()) {
                                            selectedPreset = "none"
                                        } else {
                                            selectedPreset = hex
                                            hexToRgb(hex)?.let { (r, g, b) ->
                                                sliderR = r; sliderG = g; sliderB = b
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (hex.isEmpty()) {
                                    Text(
                                        "✕",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // RGB sliders
                Text("Custom color", style = MaterialTheme.typography.bodyMedium)

                ColorSlider(
                    label = "R",
                    value = sliderR,
                    trackColor = Color(1f, 0f, 0f),
                    onValueChange = { sliderR = it; selectedPreset = "" }
                )
                ColorSlider(
                    label = "G",
                    value = sliderG,
                    trackColor = Color(0f, 0.7f, 0f),
                    onValueChange = { sliderG = it; selectedPreset = "" }
                )
                ColorSlider(
                    label = "B",
                    value = sliderB,
                    trackColor = Color(0f, 0.4f, 1f),
                    onValueChange = { sliderB = it; selectedPreset = "" }
                )

                if (selectedPreset == "") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                runCatching {
                                    Color(android.graphics.Color.parseColor(customHex))
                                }.getOrDefault(Color.Gray)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        scope.launch {
                            if (isEditing && existingCategory != null) {
                                updateCategory(
                                    existingCategory!!.copy(
                                        name = name.trim(),
                                        description = description.trim(),
                                        tags = tags,
                                        includeInNotifications = includeInNotifications,
                                        color = effectiveColor
                                    )
                                )
                            } else {
                                addCategory(
                                    Category(
                                        id = UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        description = description.trim(),
                                        tags = tags,
                                        includeInNotifications = includeInNotifications,
                                        color = effectiveColor
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

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    trackColor: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = trackColor,
            modifier = Modifier.size(16.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor,
                inactiveTrackColor = trackColor.copy(alpha = 0.3f)
            )
        )
        Text(
            text = value.toInt().toString().padStart(3),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
