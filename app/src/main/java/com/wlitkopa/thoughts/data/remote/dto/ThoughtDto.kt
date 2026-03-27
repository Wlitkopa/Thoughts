package com.wlitkopa.thoughts.data.remote.dto

import com.wlitkopa.thoughts.domain.model.Thought
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThoughtDto(
    val id: String,
    val content: String,
    val author: String = "",
    val source: String = "",
    val note: String = "",
    @SerialName("category_id") val categoryId: String,
    val tags: List<String> = emptyList(),
    @SerialName("include_in_draws") val includeInDraws: Boolean = true,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long
) {
    fun toDomain() = Thought(
        id = id,
        content = content,
        author = author,
        source = source,
        note = note,
        categoryId = categoryId,
        tags = tags,
        includeInDraws = includeInDraws,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Thought.toDto() = ThoughtDto(
    id = id,
    content = content,
    author = author,
    source = source,
    note = note,
    categoryId = categoryId,
    tags = tags,
    includeInDraws = includeInDraws,
    createdAt = createdAt,
    updatedAt = updatedAt
)
