package com.wlitkopa.thoughts.di

import com.wlitkopa.thoughts.data.local.createDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { createDatabase(androidContext()) }
}
