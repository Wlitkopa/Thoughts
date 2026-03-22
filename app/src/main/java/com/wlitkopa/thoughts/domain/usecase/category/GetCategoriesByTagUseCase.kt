package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class GetCategoriesByTagUseCase(private val repository: CategoryRepository) {
    operator fun invoke(tag: String) = repository.getByTag(tag)
}
