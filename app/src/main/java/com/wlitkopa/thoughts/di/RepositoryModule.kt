package com.wlitkopa.thoughts.di

import com.wlitkopa.thoughts.data.local.LocalCategoryRepository
import com.wlitkopa.thoughts.data.local.LocalSavedListRepository
import com.wlitkopa.thoughts.data.local.LocalThoughtRepository
import com.wlitkopa.thoughts.domain.repository.CategoryRepository
import com.wlitkopa.thoughts.domain.repository.SavedListRepository
import com.wlitkopa.thoughts.domain.repository.ThoughtRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<CategoryRepository> { LocalCategoryRepository(get()) }
    single<ThoughtRepository> { LocalThoughtRepository(get()) }
    single<SavedListRepository> { LocalSavedListRepository(get()) }
}
