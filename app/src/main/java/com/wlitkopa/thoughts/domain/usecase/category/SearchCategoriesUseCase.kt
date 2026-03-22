package com.wlitkopa.thoughts.domain.usecase.category

import com.wlitkopa.thoughts.domain.repository.CategoryRepository

class SearchCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(query: String) = repository.search(query)
}
