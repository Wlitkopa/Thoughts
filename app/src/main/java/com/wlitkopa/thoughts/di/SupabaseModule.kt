package com.wlitkopa.thoughts.di

import com.wlitkopa.thoughts.data.remote.SupabasePreferences
import com.wlitkopa.thoughts.data.remote.SupabaseSyncService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val supabaseModule = module {
    single { SupabasePreferences(androidContext()) }
    single {
        SupabaseSyncService(
            prefs = get(),
            thoughtRepository = get(),
            categoryRepository = get(),
            savedListRepository = get()
        )
    }
}
