package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class GetCategoryByIdUseCase(private val repository: CategoryRepository) {
    operator fun invoke(id: String) = repository.getById(id)
}
