package com.wlitkopa.thoughts.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.wlitkopa.thoughts.data.local.mapper.toDomain
import com.wlitkopa.thoughts.db.ThoughtsDatabase
import com.wlitkopa.thoughts.domain.model.SavedList
import com.wlitkopa.thoughts.domain.repository.SavedListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalSavedListRepository(database: ThoughtsDatabase) : SavedListRepository {

    private val queries = database.savedListEntityQueries

    override fun getAll(): Flow<List<SavedList>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getById(id: String): Flow<SavedList?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }

    override suspend fun insert(savedList: SavedList) {
        withContext(Dispatchers.IO) {
            queries.insert(
                id = savedList.id,
                name = savedList.name,
                description = savedList.description,
                filterCategoryIds = savedList.filterCategoryIds,
                filterTags = savedList.filterTags,
                filterAuthors = savedList.filterAuthors,
                filterSources = savedList.filterSources,
                pinnedThoughtIds = savedList.pinnedThoughtIds,
                createdAt = savedList.createdAt
            )
        }
    }

    override suspend fun update(savedList: SavedList) {
        withContext(Dispatchers.IO) {
            queries.update(
                name = savedList.name,
                description = savedList.description,
                filterCategoryIds = savedList.filterCategoryIds,
                filterTags = savedList.filterTags,
                filterAuthors = savedList.filterAuthors,
                filterSources = savedList.filterSources,
                pinnedThoughtIds = savedList.pinnedThoughtIds,
                id = savedList.id
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) { queries.delete(id) }
    }
}
