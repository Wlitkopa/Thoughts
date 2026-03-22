package com.wlitkopa.thoughts.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.wlitkopa.thoughts.db.CategoryEntity
import com.wlitkopa.thoughts.db.ThoughtsDatabase
import com.wlitkopa.thoughts.db.ThoughtEntity

fun createDatabase(context: Context): ThoughtsDatabase {
    val driver = AndroidSqliteDriver(
        schema = ThoughtsDatabase.Schema,
        context = context,
        name = "thoughts.db"
    )
    return ThoughtsDatabase(
        driver = driver,
        CategoryEntityAdapter = CategoryEntity.Adapter(tagsAdapter),
        ThoughtEntityAdapter = ThoughtEntity.Adapter(tagsAdapter)
    )
}
