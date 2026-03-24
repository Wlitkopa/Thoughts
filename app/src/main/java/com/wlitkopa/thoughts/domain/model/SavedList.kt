package com.wlitkopa.thoughts.domain.model

data class SavedList(
    val id: String,
    val name: String,
    val description: String = "",
    val filterCategoryIds: List<String> = emptyList(),
    val filterTags: List<String> = emptyList(),
    val filterAuthors: List<String> = emptyList(),
    val filterSources: List<String> = emptyList(),
    val pinnedThoughtIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
