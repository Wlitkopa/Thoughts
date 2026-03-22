package com.wlitkopa.thoughts.domain.model

data class Thought(
    val id: String,
    val content: String,
    val author: String = "",
    val source: String = "",
    val note: String = "",
    val categoryId: String,
    val tags: List<String> = emptyList(),
    val includeInDraws: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
