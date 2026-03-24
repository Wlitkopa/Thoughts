package com.wlitkopa.thoughts.domain.usecase.list

import com.wlitkopa.thoughts.domain.repository.SavedListRepository

class DeleteSavedListUseCase(private val repository: SavedListRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
