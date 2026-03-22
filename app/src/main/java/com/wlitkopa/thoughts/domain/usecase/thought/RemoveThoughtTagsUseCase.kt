package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class RemoveThoughtTagsUseCase(private val repository: ThoughtRepository) {
    suspend operator fun invoke(ids: List<String>, tags: List<String>) = repository.removeTags(ids, tags)
}
