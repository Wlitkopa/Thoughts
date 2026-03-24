package com.wlitkopa.thoughts.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.wlitkopa.thoughts.data.local.mapper.toDomain
import com.wlitkopa.thoughts.db.ThoughtsDatabase
import com.wlitkopa.thoughts.domain.model.Category
import com.wlitkopa.thoughts.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalCategoryRepository(database: ThoughtsDatabase) : CategoryRepository {

    private val queries = database.categoryEntityQueries

    override fun getAll(): Flow<List<Category>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getById(id: String): Flow<Category?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }

    override fun getByTag(tag: String): Flow<List<Category>> =
        queries.selectByTag(tag).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun search(query: String): Flow<List<Category>> =
        queries.search(query).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getAllTags(): Flow<List<String>> =
        queries.selectAllTagStrings().asFlow().mapToList(Dispatchers.IO)
            .map { tagLists -> tagLists.flatten().distinct().sorted() }

    override suspend fun updateTags(ids: List<String>, tags: List<String>) {
        withContext(Dispatchers.IO) {
            queries.updateTags(tags = tags, ids = ids)
        }
    }

    override suspend fun removeTags(ids: List<String>, tags: List<String>) {
        withContext(Dispatchers.IO) {
            val tagsToRemove = tags.toSet()
            ids.forEach { id ->
                val current = queries.selectById(id).executeAsOneOrNull()?.tags ?: return@forEach
                val updated = current.filterNot { it in tagsToRemove }
                queries.updateTags(tags = updated, ids = listOf(id))
            }
        }
    }

    override suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            queries.insert(
                id = category.id,
                name = category.name,
                description = category.description,
                tags = category.tags,
                includeInNotifications = if (category.includeInNotifications) 1L else 0L,
                createdAt = category.createdAt,
                color = category.color
            )
        }
    }

    override suspend fun update(category: Category) {
        withContext(Dispatchers.IO) {
            queries.update(
                name = category.name,
                description = category.description,
                tags = category.tags,
                includeInNotifications = if (category.includeInNotifications) 1L else 0L,
                color = category.color,
                id = category.id
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            queries.delete(id)
        }
    }
}
