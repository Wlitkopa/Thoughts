package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class ClearThoughtTagsUseCase(private val repository: ThoughtRepository) {
    suspend operator fun invoke(ids: List<String>) = repository.clearTags(ids)
}
