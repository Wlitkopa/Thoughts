package com.wlitkopa.thoughts.notification

import android.content.Context

private const val PREFS_NAME = "notification_prefs"
private const val KEY_ENABLED = "enabled"
private const val KEY_INTERVAL_HOURS = "interval_hours"
private const val KEY_START_HOUR = "start_hour"
private const val KEY_START_MINUTE = "start_minute"
private const val KEY_CATEGORY_IDS = "category_ids"
private const val KEY_TAGS = "tags"

class NotificationPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var intervalHours: Int
        get() = prefs.getInt(KEY_INTERVAL_HOURS, 24)
        set(value) = prefs.edit().putInt(KEY_INTERVAL_HOURS, value).apply()

    var startHour: Int
        get() = prefs.getInt(KEY_START_HOUR, 8)
        set(value) = prefs.edit().putInt(KEY_START_HOUR, value).apply()

    var startMinute: Int
        get() = prefs.getInt(KEY_START_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_START_MINUTE, value).apply()

    var categoryIds: Set<String>
        get() = prefs.getStringSet(KEY_CATEGORY_IDS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_CATEGORY_IDS, value).apply()

    var tags: Set<String>
        get() = prefs.getStringSet(KEY_TAGS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_TAGS, value).apply()
}
