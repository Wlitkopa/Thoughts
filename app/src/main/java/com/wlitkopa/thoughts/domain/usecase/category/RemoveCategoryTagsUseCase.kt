package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class RemoveCategoryTagsUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(ids: List<String>, tags: List<String>) = repository.removeTags(ids, tags)
}
