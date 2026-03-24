package com.wlitkopa.thoughts.domain.model

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val includeInNotifications: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val color: String = ""
)
