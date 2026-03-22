package com.wlitkopa.thoughts

import android.app.Application
import com.wlitkopa.thoughts.di.databaseModule
import com.wlitkopa.thoughts.di.repositoryModule
import com.wlitkopa.thoughts.di.useCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ThoughtsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ThoughtsApp)
            modules(databaseModule, repositoryModule, useCaseModule)
        }
    }
}
