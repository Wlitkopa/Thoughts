package com.wlitkopa.thoughts.data.local

import app.cash.sqldelight.ColumnAdapter

val tagsAdapter = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> {
        if (databaseValue.isEmpty()) return emptyList()
        return databaseValue.trim('|').split('|').filter { it.isNotBlank() }
    }

    override fun encode(value: List<String>): String {
        if (value.isEmpty()) return ""
        return "|${value.joinToString("|")}|"
    }
}
