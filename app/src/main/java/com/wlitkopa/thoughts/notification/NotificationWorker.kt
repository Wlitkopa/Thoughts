package com.wlitkopa.thoughts.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wlitkopa.thoughts.domain.usecase.thought.GetRandomThoughtUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val getRandomThought: GetRandomThoughtUseCase by inject()

    override suspend fun doWork(): Result {
        val prefs = NotificationPreferences(context)

        val thought = getRandomThought(
            categoryIds = prefs.categoryIds.toList(),
            tags = prefs.tags.toList()
        ) ?: return Result.success()

        showThoughtNotification(context, thought)
        return Result.success()
    }
}
