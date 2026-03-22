package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
