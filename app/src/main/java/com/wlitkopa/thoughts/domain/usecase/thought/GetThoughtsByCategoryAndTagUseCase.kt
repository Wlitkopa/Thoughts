package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetThoughtsByCategoryAndTagUseCase(private val repository: ThoughtRepository) {
    operator fun invoke(categoryId: String, tag: String) = repository.getByCategoryAndTag(categoryId, tag)
}
