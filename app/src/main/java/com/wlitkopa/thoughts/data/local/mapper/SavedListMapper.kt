package com.wlitkopa.thoughts.data.local.mapper

import com.wlitkopa.thoughts.db.SavedListEntity
import com.wlitkopa.thoughts.domain.model.SavedList

fun SavedListEntity.toDomain() = SavedList(
    id = id,
    name = name,
    description = description,
    filterCategoryIds = filter_category_ids,
    filterTags = filter_tags,
    filterAuthors = filter_authors,
    filterSources = filter_sources,
    pinnedThoughtIds = pinned_thought_ids,
    createdAt = created_at
)
