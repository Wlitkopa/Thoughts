package com.wlitkopa.thoughts.domain.usecase.list

import com.wlitkopa.thoughts.domain.model.SavedList
import com.wlitkopa.thoughts.domain.repository.SavedListRepository

class AddSavedListUseCase(private val repository: SavedListRepository) {
    suspend operator fun invoke(savedList: SavedList) = repository.insert(savedList)
}
