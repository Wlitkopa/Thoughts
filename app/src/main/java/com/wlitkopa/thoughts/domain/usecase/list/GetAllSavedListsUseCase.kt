package com.wlitkopa.thoughts.domain.usecase.list

import com.wlitkopa.thoughts.domain.repository.SavedListRepository

class GetAllSavedListsUseCase(private val repository: SavedListRepository) {
    operator fun invoke() = repository.getAll()
}
