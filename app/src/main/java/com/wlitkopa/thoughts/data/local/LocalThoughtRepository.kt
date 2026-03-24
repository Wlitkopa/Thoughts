package com.wlitkopa.thoughts.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.wlitkopa.thoughts.data.local.mapper.toDomain
import com.wlitkopa.thoughts.db.ThoughtsDatabase
import com.wlitkopa.thoughts.domain.model.Thought
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalThoughtRepository(database: ThoughtsDatabase) : ThoughtRepository {

    private val queries = database.thoughtEntityQueries

    override fun getAll(): Flow<List<Thought>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getById(id: String): Flow<Thought?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }

    override fun getByCategory(categoryId: String): Flow<List<Thought>> =
        queries.selectByCategory(categoryId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getByCategories(categoryIds: List<String>): Flow<List<Thought>> =
        queries.selectByCategories(categoryIds).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getByTag(tag: String): Flow<List<Thought>> =
        queries.selectByTag(tag).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getByCategoryAndTag(categoryId: String, tag: String): Flow<List<Thought>> =
        queries.selectByCategoryAndTag(categoryId, tag).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun search(query: String): Flow<List<Thought>> =
        queries.search(query).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }

    override fun getAllTags(): Flow<List<String>> =
        queries.selectAllTagStrings().asFlow().mapToList(Dispatchers.IO)
            .map { tagLists -> tagLists.flatten().distinct().sorted() }

    override fun getAllAuthors(): Flow<List<String>> =
        queries.selectAllAuthors().asFlow().mapToList(Dispatchers.IO)

    override fun getAllSources(): Flow<List<String>> =
        queries.selectAllSources().asFlow().mapToList(Dispatchers.IO)

    override suspend fun getRandom(categoryIds: List<String>, tags: List<String>): Thought? {
        return withContext(Dispatchers.IO) {
            when {
                categoryIds.isEmpty() && tags.isEmpty() ->
                    queries.getRandomAll().executeAsOneOrNull()?.toDomain()

                categoryIds.isNotEmpty() && tags.isEmpty() ->
                    queries.getRandomByCategories(categoryIds).executeAsOneOrNull()?.toDomain()

                categoryIds.isEmpty() && tags.isNotEmpty() ->
                    // Dla wielu tagów losujemy jeden z nich i szukamy losowej myśli z tym tagiem
                    tags.shuffled().firstNotNullOfOrNull { tag ->
                        queries.getRandomByTags(tag).executeAsOneOrNull()?.toDomain()
                    }

                else ->
                    tags.shuffled().firstNotNullOfOrNull { tag ->
                        queries.getRandomByCategoriesAndTags(categoryIds, tag).executeAsOneOrNull()?.toDomain()
                    }
            }
        }
    }

    override suspend fun updateTags(ids: List<String>, tags: List<String>) {
        withContext(Dispatchers.IO) {
            queries.updateTags(
                tags = tags,
                updatedAt = System.currentTimeMillis(),
                ids = ids
            )
        }
    }

    override suspend fun removeTags(ids: List<String>, tags: List<String>) {
        withContext(Dispatchers.IO) {
            val tagsToRemove = tags.toSet()
            ids.forEach { id ->
                val current = queries.selectById(id).executeAsOneOrNull()?.tags ?: return@forEach
                val updated = current.filterNot { it in tagsToRemove }
                queries.updateTags(
                    tags = updated,
                    updatedAt = System.currentTimeMillis(),
                    ids = listOf(id)
                )
            }
        }
    }

    override suspend fun insert(thought: Thought) {
        withContext(Dispatchers.IO) {
            queries.insert(
                id = thought.id,
                content = thought.content,
                author = thought.author,
                source = thought.source,
                note = thought.note,
                categoryId = thought.categoryId,
                tags = thought.tags,
                includeInDraws = if (thought.includeInDraws) 1L else 0L,
                createdAt = thought.createdAt,
                updatedAt = thought.updatedAt
            )
        }
    }

    override suspend fun update(thought: Thought) {
        withContext(Dispatchers.IO) {
            queries.update(
                content = thought.content,
                author = thought.author,
                source = thought.source,
                note = thought.note,
                categoryId = thought.categoryId,
                tags = thought.tags,
                includeInDraws = if (thought.includeInDraws) 1L else 0L,
                updatedAt = System.currentTimeMillis(),
                id = thought.id
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            queries.delete(id)
        }
    }
}
