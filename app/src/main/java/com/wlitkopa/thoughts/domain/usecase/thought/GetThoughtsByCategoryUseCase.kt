package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetThoughtsByCategoryUseCase(private val repository: ThoughtRepository) {
    operator fun invoke(categoryId: String) = repository.getByCategory(categoryId)
}
