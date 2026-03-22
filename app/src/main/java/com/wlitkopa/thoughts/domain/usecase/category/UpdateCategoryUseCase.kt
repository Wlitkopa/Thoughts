package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.model.Category
import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class UpdateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.update(category)
}
