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
    private val scheduler: NotificationScheduler by inject()

    override suspend fun doWork(): Result {
        val cronExpression = inputData.getString(KEY_CRON_EXPRESSION)
            ?: return Result.success()

        val prefs = NotificationPreferences(context)
        if (!prefs.isEnabled) return Result.success()

        val thought = getRandomThought(
            categoryIds = prefs.categoryIds.toList(),
            tags = prefs.tags.toList()
        )
        if (thought != null) {
            showThoughtNotification(context, thought)
        }

        scheduler.schedule(cronExpression)

        return Result.success()
    }
}
