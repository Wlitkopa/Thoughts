package com.wlitkopa.thoughts.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val KEY_CRON_EXPRESSION = "cron_expression"
private const val WORK_NAME = "thought_notification"

class NotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun schedule(cronExpression: String) {
        val next = CronParser.nextExecution(cronExpression) ?: return
        val delayMs = next.timeInMillis - Calendar.getInstance().timeInMillis

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_CRON_EXPRESSION to cronExpression))
            .build()

        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }
}
