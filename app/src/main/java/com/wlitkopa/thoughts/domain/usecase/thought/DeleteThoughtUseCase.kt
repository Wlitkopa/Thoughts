package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class DeleteThoughtUseCase(private val repository: ThoughtRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
