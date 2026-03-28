package com.wlitkopa.thoughts.data.remote.dto

import com.wlitkopa.thoughts.domain.model.SavedList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedListDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("filter_category_ids") val filterCategoryIds: List<String>,
    @SerialName("filter_tags") val filterTags: List<String>,
    @SerialName("filter_authors") val filterAuthors: List<String>,
    @SerialName("filter_sources") val filterSources: List<String>,
    @SerialName("pinned_thought_ids") val pinnedThoughtIds: List<String>,
    @SerialName("created_at") val createdAt: Long
) {
    fun toDomain() = SavedList(
        id = id,
        name = name,
        description = description,
        filterCategoryIds = filterCategoryIds,
        filterTags = filterTags,
        filterAuthors = filterAuthors,
        filterSources = filterSources,
        pinnedThoughtIds = pinnedThoughtIds,
        createdAt = createdAt
    )
}

fun SavedList.toDto() = SavedListDto(
    id = id,
    name = name,
    description = description,
    filterCategoryIds = filterCategoryIds,
    filterTags = filterTags,
    filterAuthors = filterAuthors,
    filterSources = filterSources,
    pinnedThoughtIds = pinnedThoughtIds,
    createdAt = createdAt
)
