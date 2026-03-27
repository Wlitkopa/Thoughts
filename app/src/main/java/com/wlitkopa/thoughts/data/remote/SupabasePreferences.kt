package com.wlitkopa.thoughts.data.remote

import android.content.Context

class SupabasePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)

    var url: String
        get() = prefs.getString("url", "") ?: ""
        set(value) { prefs.edit().putString("url", value).apply() }

    var anonKey: String
        get() = prefs.getString("anon_key", "") ?: ""
        set(value) { prefs.edit().putString("anon_key", value).apply() }

    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()
}
