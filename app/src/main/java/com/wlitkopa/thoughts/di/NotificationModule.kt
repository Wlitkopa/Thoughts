package com.wlitkopa.thoughts.di

import com.wlitkopa.thoughts.notification.NotificationPreferences
import com.wlitkopa.thoughts.notification.NotificationScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationPreferences(androidContext()) }
    single { NotificationScheduler(androidContext()) }
}
