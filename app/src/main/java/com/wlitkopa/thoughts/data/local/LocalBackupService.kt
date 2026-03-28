package com.wlitkopa.thoughts.data.local

import com.wlitkopa.thoughts.data.remote.dto.toDto
import com.wlitkopa.thoughts.domain.repository.CategoryRepository
import com.wlitkopa.thoughts.domain.repository.SavedListRepository
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalBackupService(
    private val thoughtRepository: ThoughtRepository,
    private val categoryRepository: CategoryRepository,
    private val savedListRepository: SavedListRepository
) {
    private val json = Json { prettyPrint = true }

    suspend fun createBackup(): String = withContext(Dispatchers.IO) {
        val backup = BackupData(
            categories = categoryRepository.getAll().first().map { it.toDto() },
            thoughts = thoughtRepository.getAll().first().map { it.toDto() },
            savedLists = savedListRepository.getAll().first().map { it.toDto() }
        )
        json.encodeToString(backup)
    }

    // Returns Triple(categories, thoughts, savedLists) counts of imported records
    suspend fun restoreBackup(jsonString: String): Triple<Int, Int, Int> = withContext(Dispatchers.IO) {
        val backup = Json.decodeFromString<BackupData>(jsonString)

        backup.categories.forEach { dto ->
            runCatching { categoryRepository.insert(dto.toDomain()) }
                .onFailure { runCatching { categoryRepository.update(dto.toDomain()) } }
        }
        backup.thoughts.forEach { dto ->
            runCatching { thoughtRepository.insert(dto.toDomain()) }
                .onFailure { runCatching { thoughtRepository.update(dto.toDomain()) } }
        }
        backup.savedLists.forEach { dto ->
            runCatching { savedListRepository.insert(dto.toDomain()) }
                .onFailure { runCatching { savedListRepository.update(dto.toDomain()) } }
        }

        Triple(backup.categories.size, backup.thoughts.size, backup.savedLists.size)
    }
}
