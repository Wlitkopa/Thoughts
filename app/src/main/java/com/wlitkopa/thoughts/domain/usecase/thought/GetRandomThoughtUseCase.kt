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
        tags: List<String> = emptyList(),
        excludeId: String = ""
    ): com.wlitkopa.thoughts.domain.model.Thought? {
        val activeIds = categoryRepository.getAll().first()
            .filter { it.includeInNotifications }
            .map { it.id }

        val effectiveIds = categoryIds.filter { it in activeIds }

        // User explicitly selected categories but none are dice-ON → nothing to draw
        if (categoryIds.isNotEmpty() && effectiveIds.isEmpty()) return null

        // If no dice-ON categories exist at all → nothing to draw
        if (categoryIds.isEmpty() && activeIds.isEmpty()) return null

        // Pass effectiveIds (validated selection) or empty list if user selected nothing
        return thoughtRepository.getRandom(effectiveIds, tags, excludeId)
    }
}
