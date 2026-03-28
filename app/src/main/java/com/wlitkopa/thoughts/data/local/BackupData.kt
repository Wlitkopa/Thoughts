package com.wlitkopa.thoughts.data.local

import com.wlitkopa.thoughts.data.remote.dto.CategoryDto
import com.wlitkopa.thoughts.data.remote.dto.SavedListDto
import com.wlitkopa.thoughts.data.remote.dto.ThoughtDto
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val categories: List<CategoryDto> = emptyList(),
    val thoughts: List<ThoughtDto> = emptyList(),
    val savedLists: List<SavedListDto> = emptyList()
)
