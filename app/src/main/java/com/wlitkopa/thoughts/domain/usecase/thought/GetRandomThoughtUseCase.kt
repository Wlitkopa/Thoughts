package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.CategoryRepository
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository
import kotlinx.coroutines.flow.first

class GetRandomThoughtUseCase(
    private val thoughtRepository: ThoughtRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        categoryIds: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): com.wlitkopa.thoughts.domain.model.Thought? {
        // Categories with includeInNotifications = false are excluded from draws
        val activeIds = categoryRepository.getAll().first()
            .filter { it.includeInNotifications }
            .map { it.id }

        val effectiveIds = if (categoryIds.isEmpty()) {
            activeIds
        } else {
            categoryIds.filter { it in activeIds }
        }

        // All categories excluded → nothing to draw
        if (effectiveIds.isEmpty()) return null

        return thoughtRepository.getRandom(effectiveIds, tags)
    }
}
