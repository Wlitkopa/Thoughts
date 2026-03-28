package com.wlitkopa.thoughts.data.remote.dto

import com.wlitkopa.thoughts.domain.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String>,
    @SerialName("include_in_notifications") val includeInNotifications: Boolean,
    @SerialName("created_at") val createdAt: Long,
    val color: String
) {
    fun toDomain() = Category(
        id = id,
        name = name,
        description = description,
        tags = tags,
        includeInNotifications = includeInNotifications,
        createdAt = createdAt,
        color = color
    )
}

fun Category.toDto() = CategoryDto(
    id = id,
    name = name,
    description = description,
    tags = tags,
    includeInNotifications = includeInNotifications,
    createdAt = createdAt,
    color = color
)
