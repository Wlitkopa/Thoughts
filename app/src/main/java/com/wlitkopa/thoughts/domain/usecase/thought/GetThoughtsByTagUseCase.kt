package com.wlitkopa.thoughts.domain.usecase.thought

import com.wlitkopa.thoughts.domain.repository.ThoughtRepository

class GetThoughtsByTagUseCase(private val repository: ThoughtRepository) {
    operator fun invoke(tag: String) = repository.getByTag(tag)
}
