package com.wlitkopa.thoughts.data.local.mapper

import com.wlitkopa.thoughts.db.ThoughtEntity
import com.wlitkopa.thoughts.domain.model.Thought

fun ThoughtEntity.toDomain() = Thought(
    id = id,
    content = content,
    author = author,
    source = source,
    note = note,
    categoryId = category_id,
    tags = tags,
    includeInDraws = include_in_draws == 1L,
    createdAt = created_at,
    updatedAt = updated_at
)
