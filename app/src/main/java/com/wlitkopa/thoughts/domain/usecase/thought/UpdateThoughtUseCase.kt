package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class UpdateThoughtUseCase(private val repository: ThoughtRepository) {
    suspend operator fun invoke(thought: Thought) = repository.update(thought)
}
