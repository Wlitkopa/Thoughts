package com.wlitkopa.thoughts.domain.usecase.list

import com.wlitkopa.thoughts.domain.repository.SavedListRepository

class GetSavedListByIdUseCase(private val repository: SavedListRepository) {
    operator fun invoke(id: String) = repository.getById(id)
}
