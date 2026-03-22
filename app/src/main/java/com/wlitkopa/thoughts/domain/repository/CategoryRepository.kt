package com.wlitkopa.thoughts.domain.repository

import com.wlitkopa.thoughts.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getById(id: String): Flow<Category?>
    fun getByTag(tag: String): Flow<List<Category>>
    fun search(query: String): Flow<List<Category>>
    fun getAllTags(): Flow<List<String>>
    suspend fun updateTags(ids: List<String>, tags: List<String>)
    suspend fun clearTags(ids: List<String>)
    suspend fun insert(category: Category)
    suspend fun update(category: Category)
    suspend fun delete(id: String)
}
