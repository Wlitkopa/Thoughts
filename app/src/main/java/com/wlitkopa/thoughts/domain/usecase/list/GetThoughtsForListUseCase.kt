package com.wlitkopa.thoughts.domain.usecase.list

import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.repository.SavedListRepository
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetThoughtsForListUseCase(
    private val thoughtRepository: ThoughtRepository,
    private val listRepository: SavedListRepository
) {
    operator fun invoke(listId: String): Flow<List<Thought>> =
        listRepository.getById(listId).flatMapLatest { savedList ->
            if (savedList == null) return@flatMapLatest flowOf(emptyList())
            thoughtRepository.getAll().map { thoughts ->
                thoughts.filter { thought ->
                    val isPinned = thought.id in savedList.pinnedThoughtIds
                    val inCategory = savedList.filterCategoryIds.isNotEmpty() &&
                            thought.categoryId in savedList.filterCategoryIds
                    val inTag = savedList.filterTags.isNotEmpty() &&
                            savedList.filterTags.any { tag -> tag in thought.tags }
                    val inAuthor = savedList.filterAuthors.isNotEmpty() &&
                            thought.author in savedList.filterAuthors
                    val inSource = savedList.filterSources.isNotEmpty() &&
                            thought.source in savedList.filterSources
                    isPinned || inCategory || inTag || inAuthor || inSource
                }.sortedByDescending { it.createdAt }
            }
        }
}
