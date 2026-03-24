package com.wlitkopa.thoughts.domain.repository

import com.wlitkopa.thoughts.domain.model.Thought
import kotlinx.coroutines.flow.Flow

interface ThoughtRepository {
    fun getAll(): Flow<List<Thought>>
    fun getById(id: String): Flow<Thought?>
    fun getByCategory(categoryId: String): Flow<List<Thought>>
    fun getByCategories(categoryIds: List<String>): Flow<List<Thought>>
    fun getByTag(tag: String): Flow<List<Thought>>
    fun getByCategoryAndTag(categoryId: String, tag: String): Flow<List<Thought>>
    fun search(query: String): Flow<List<Thought>>
    fun getAllTags(): Flow<List<String>>
    fun getAllAuthors(): Flow<List<String>>
    fun getAllSources(): Flow<List<String>>
    suspend fun getRandom(categoryIds: List<String> = emptyList(), tags: List<String> = emptyList()): Thought?
    suspend fun updateTags(ids: List<String>, tags: List<String>)
    suspend fun removeTags(ids: List<String>, tags: List<String>)
    suspend fun insert(thought: Thought)
    suspend fun update(thought: Thought)
    suspend fun delete(id: String)
}
