package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class GetAllCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke() = repository.getAll()
}
