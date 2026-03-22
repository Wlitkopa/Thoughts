package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetRandomThoughtUseCase(private val repository: ThoughtRepository) {
    suspend operator fun invoke(
        categoryIds: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ) = repository.getRandom(categoryIds, tags)
}
