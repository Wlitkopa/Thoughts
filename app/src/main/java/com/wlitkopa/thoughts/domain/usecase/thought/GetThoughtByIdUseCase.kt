package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetThoughtByIdUseCase(private val repository: ThoughtRepository) {
    operator fun invoke(id: String) = repository.getById(id)
}
