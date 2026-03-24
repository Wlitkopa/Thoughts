package com.wlitkopa.thoughts.data.local.mapper

import com.wlitkopa.thoughts.db.CategoryEntity
import com.wlitkopa.thoughts.domain.model.Category

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    description = description,
    tags = tags,
    includeInNotifications = include_in_notifications == 1L,
    createdAt = created_at,
    color = color
)

fun Category.toInsertParams() = listOf(
    id, name, description, tags,
    if (includeInNotifications) 1L else 0L,
    createdAt
)
