package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class ClearCategoryTagsUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(ids: List<String>) = repository.clearTags(ids)
}
