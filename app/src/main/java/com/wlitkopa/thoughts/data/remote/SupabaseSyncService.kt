package com.wlitkopa.thoughts.data.remote

import com.wlitkopa.thoughts.data.remote.dto.CategoryDto
import com.wlitkopa.thoughts.data.remote.dto.SavedListDto
import com.wlitkopa.thoughts.data.remote.dto.ThoughtDto
import com.wlitkopa.thoughts.data.remote.dto.toDto
import com.wlitkopa.thoughts.domain.repository.CategoryRepository
import com.wlitkopa.thoughts.domain.repository.SavedListRepository
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SupabaseSyncService(
    private val prefs: SupabasePreferences,
    private val thoughtRepository: ThoughtRepository,
    private val categoryRepository: CategoryRepository,
    private val savedListRepository: SavedListRepository
) {
    private fun buildClient() = createSupabaseClient(prefs.url.trim(), prefs.anonKey.trim()) {
        install(Postgrest)
    }

    suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!prefs.isConfigured) error("URL and anon key must be set first.")
            val client = buildClient()
            client.from("categories").select()
            Unit
        }
    }

    suspend fun upload(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!prefs.isConfigured) error("URL and anon key must be set first.")
            val client = buildClient()

            val categories = categoryRepository.getAll().first().map { it.toDto() }
            val thoughts = thoughtRepository.getAll().first().map { it.toDto() }
            val lists = savedListRepository.getAll().first().map { it.toDto() }

            val localCatIds = categories.map { it.id }.toSet()
            val localThoughtIds = thoughts.map { it.id }.toSet()
            val localListIds = lists.map { it.id }.toSet()

            // Delete remote records no longer present locally
            val remoteCatIds = client.from("categories").select().decodeList<CategoryDto>().map { it.id }
            val remoteThoughtIds = client.from("thoughts").select().decodeList<ThoughtDto>().map { it.id }
            val remoteListIds = client.from("saved_lists").select().decodeList<SavedListDto>().map { it.id }

            remoteCatIds.filterNot { it in localCatIds }.forEach { id ->
                client.from("categories").delete { filter { eq("id", id) } }
            }
            remoteThoughtIds.filterNot { it in localThoughtIds }.forEach { id ->
                client.from("thoughts").delete { filter { eq("id", id) } }
            }
            remoteListIds.filterNot { it in localListIds }.forEach { id ->
                client.from("saved_lists").delete { filter { eq("id", id) } }
            }

            if (categories.isNotEmpty()) client.from("categories").upsert(categories)
            if (thoughts.isNotEmpty()) client.from("thoughts").upsert(thoughts)
            if (lists.isNotEmpty()) client.from("saved_lists").upsert(lists)
        }
    }

    // Imports remote data into local DB (merge: add/update, never delete local-only records)
    suspend fun download(): Result<Triple<Int, Int, Int>> = withContext(Dispatchers.IO) {
        runCatching {
            if (!prefs.isConfigured) error("URL and anon key must be set first.")
            val client = buildClient()

            // Categories first — thoughts reference them via category_id
            val remoteCategories = client.from("categories").select().decodeList<CategoryDto>()
            val remoteThoughts = client.from("thoughts").select().decodeList<ThoughtDto>()
            val remoteLists = client.from("saved_lists").select().decodeList<SavedListDto>()

            remoteCategories.forEach { dto ->
                runCatching { categoryRepository.insert(dto.toDomain()) }
                    .onFailure { runCatching { categoryRepository.update(dto.toDomain()) } }
            }
            remoteThoughts.forEach { dto ->
                runCatching { thoughtRepository.insert(dto.toDomain()) }
                    .onFailure { runCatching { thoughtRepository.update(dto.toDomain()) } }
            }
            remoteLists.forEach { dto ->
                runCatching { savedListRepository.insert(dto.toDomain()) }
                    .onFailure { runCatching { savedListRepository.update(dto.toDomain()) } }
            }

            Triple(remoteCategories.size, remoteThoughts.size, remoteLists.size)
        }
    }
}
