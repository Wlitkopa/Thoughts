package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class SearchThoughtsUseCase(private val repository: ThoughtRepository) {
    operator fun invoke(query: String) = repository.search(query)
}
