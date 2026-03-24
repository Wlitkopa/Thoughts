package com.wlitkopa.thoughts.notification

import android.content.Context

private const val PREFS_NAME = "notification_prefs"
private const val KEY_ENABLED = "enabled"
private const val KEY_CRON = "cron_expression"
private const val KEY_CATEGORY_IDS = "category_ids"
private const val KEY_TAGS = "tags"

const val DEFAULT_CRON = "0 8 * * *" // every day at 08:00

class NotificationPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var cronExpression: String
        get() = prefs.getString(KEY_CRON, DEFAULT_CRON) ?: DEFAULT_CRON
        set(value) = prefs.edit().putString(KEY_CRON, value).apply()

    var categoryIds: Set<String>
        get() = prefs.getStringSet(KEY_CATEGORY_IDS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_CATEGORY_IDS, value).apply()

    var tags: Set<String>
        get() = prefs.getStringSet(KEY_TAGS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_TAGS, value).apply()
}
