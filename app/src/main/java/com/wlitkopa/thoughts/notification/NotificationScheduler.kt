package com.wlitkopa.thoughts.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "thought_notification"

class NotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun schedule(intervalHours: Int, startHour: Int, startMinute: Int) {
        val initialDelay = calculateInitialDelay(startHour, startMinute, intervalHours)

        val request = PeriodicWorkRequestBuilder<NotificationWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    /**
     * Oblicza czas (w ms) do następnego wystąpienia startHour:startMinute.
     * Jeśli ten czas już minął dziś, szuka kolejnego wystąpienia co intervalHours.
     * Np. teraz 10:00, start 8:00, interval 24h → następny raz jutro o 8:00
     * Np. teraz 10:00, start 8:00, interval 12h → następny raz dziś o 20:00
     */
    private fun calculateInitialDelay(startHour: Int, startMinute: Int, intervalHours: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (target.before(now)) {
            target.add(Calendar.HOUR_OF_DAY, intervalHours)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
