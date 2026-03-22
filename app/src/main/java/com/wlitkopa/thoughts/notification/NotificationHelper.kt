package com.wlitkopa.thoughts.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.wlitkopa.thoughts.R
import com.wlitkopa.thoughts.domain.model.Thought

const val CHANNEL_ID = "thoughts_channel"
const val CHANNEL_NAME = "Losowe myśli"
private const val NOTIFICATION_ID = 1001

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Cykliczne losowanie myśli i cytatów"
    }
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

fun showThoughtNotification(context: Context, thought: Thought) {
    val title = when {
        thought.author.isNotBlank() && thought.source.isNotBlank() -> "${thought.author} — ${thought.source}"
        thought.author.isNotBlank() -> thought.author
        thought.source.isNotBlank() -> thought.source
        else -> CHANNEL_NAME
    }

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
