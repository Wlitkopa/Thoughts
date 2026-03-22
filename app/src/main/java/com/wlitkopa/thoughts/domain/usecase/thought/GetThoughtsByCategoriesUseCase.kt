package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetThoughtsByCategoriesUseCase(private val repository: ThoughtRepository) {
    operator fun invoke(categoryIds: List<String>) = repository.getByCategories(categoryIds)
}
