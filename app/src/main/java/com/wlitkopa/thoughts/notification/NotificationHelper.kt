package com.wlitkopa.thoughts.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.wlitkopa.thoughts.R
import com.wlitkopa.thoughts.domain.model.Thought

const val CHANNEL_ID = "thoughts_channel"
private const val CHANNEL_NAME = "Thoughts"
private const val NOTIFICATION_ID = 1001

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Periodic random thought notifications"
    }
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

fun showThoughtNotification(context: Context, thought: Thought) {
    val title = listOf(thought.author, thought.source)
        .filter { it.isNotBlank() }
        .joinToString(" — ")
        .ifBlank { "Thoughts" }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(thought.content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(thought.content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(NOTIFICATION_ID, notification)
}
