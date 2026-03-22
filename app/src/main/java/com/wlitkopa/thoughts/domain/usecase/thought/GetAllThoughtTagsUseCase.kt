package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetAllThoughtTagsUseCase(private val repository: ThoughtRepository) {
    operator fun invoke() = repository.getAllTags()
}
