package com.wlitkopa.thoughts.di

import com.wlitkopa.thoughts.domain.usecase.category.AddCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.category.DeleteCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetAllCategoryTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetCategoriesByTagUseCase
import com.wlitkopa.thoughts.domain.usecase.category.GetCategoryByIdUseCase
import com.wlitkopa.thoughts.domain.usecase.category.RemoveCategoryTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.category.SearchCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.category.UpdateCategoryTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.category.UpdateCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.AddThoughtUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.DeleteThoughtUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetAllThoughtsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetRandomThoughtUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetThoughtsByCategoryAndTagUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetThoughtByIdUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetThoughtsByCategoriesUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetThoughtsByCategoryUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.GetThoughtsByTagUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.RemoveThoughtTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.SearchThoughtsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.UpdateThoughtTagsUseCase
import com.wlitkopa.thoughts.domain.usecase.thought.UpdateThoughtUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Category
    factory { GetAllCategoriesUseCase(get()) }
    factory { GetCategoryByIdUseCase(get()) }
    factory { GetCategoriesByTagUseCase(get()) }
    factory { SearchCategoriesUseCase(get()) }
    factory { GetAllCategoryTagsUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }
    factory { UpdateCategoryTagsUseCase(get()) }
    factory { RemoveCategoryTagsUseCase(get()) }

    // Thought
    factory { GetAllThoughtsUseCase(get()) }
    factory { GetThoughtByIdUseCase(get()) }
    factory { GetThoughtsByCategoryUseCase(get()) }
    factory { GetThoughtsByCategoriesUseCase(get()) }
    factory { GetThoughtsByTagUseCase(get()) }
    factory { GetThoughtsByCategoryAndTagUseCase(get()) }
    factory { SearchThoughtsUseCase(get()) }
    factory { GetAllThoughtTagsUseCase(get()) }
    factory { GetRandomThoughtUseCase(get(), get()) }
    factory { AddThoughtUseCase(get()) }
    factory { UpdateThoughtUseCase(get()) }
    factory { DeleteThoughtUseCase(get()) }
    factory { UpdateThoughtTagsUseCase(get()) }
    factory { RemoveThoughtTagsUseCase(get()) }
}
