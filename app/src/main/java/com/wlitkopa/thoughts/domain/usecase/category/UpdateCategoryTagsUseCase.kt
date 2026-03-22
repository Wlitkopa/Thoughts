package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class UpdateCategoryTagsUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(ids: List<String>, tags: List<String>) = repository.updateTags(ids, tags)
}
