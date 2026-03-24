package com.wlitkopa.thoughts.domain.repository

import com.wlitkopa.thoughts.domain.model.SavedList
import kotlinx.coroutines.flow.Flow

interface SavedListRepository {
    fun getAll(): Flow<List<SavedList>>
    fun getById(id: String): Flow<SavedList?>
    suspend fun insert(savedList: SavedList)
    suspend fun update(savedList: SavedList)
    suspend fun delete(id: String)
}
