package com.booru.app.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized preference manager for API keys and app settings
 */
object Prefs {

    private const val PREFS_NAME = "booru_app_prefs"

    // Keys
    private const val KEY_GELBOORU_USER_ID = "gelbooru_user_id"
    private const val KEY_GELBOORU_API_KEY = "gelbooru_api_key"
    private const val KEY_RULE34_USER_ID = "rule34_user_id"
    private const val KEY_RULE34_API_KEY = "rule34_api_key"
    private const val KEY_ACTIVE_SOURCE = "active_source" // "gelbooru" or "rule34"
    private const val KEY_LAST_SEARCH = "last_search_query"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Gelbooru credentials
    fun getGelbooruUserId(context: Context): String {
        return getPrefs(context).getString(KEY_GELBOORU_USER_ID, "") ?: ""
    }

    fun setGelbooruUserId(context: Context, userId: String) {
        getPrefs(context).edit().putString(KEY_GELBOORU_USER_ID, userId).apply()
    }

    fun getGelbooruApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GELBOORU_API_KEY, "") ?: ""
    }

    fun setGelbooruApiKey(context: Context, apiKey: String) {
        getPrefs(context).edit().putString(KEY_GELBOORU_API_KEY, apiKey).apply()
    }

    // Rule34 credentials
    fun getRule34UserId(context: Context): String {
        return getPrefs(context).getString(KEY_RULE34_USER_ID, "") ?: ""
    }

    fun setRule34UserId(context: Context, userId: String) {
        getPrefs(context).edit().putString(KEY_RULE34_USER_ID, userId).apply()
    }

    fun getRule34ApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_RULE34_API_KEY, "") ?: ""
    }

    fun setRule34ApiKey(context: Context, apiKey: String) {
        getPrefs(context).edit().putString(KEY_RULE34_API_KEY, apiKey).apply()
    }

    // Active source
    fun getActiveSource(context: Context): String {
        return getPrefs(context).getString(KEY_ACTIVE_SOURCE, "gelbooru") ?: "gelbooru"
    }

    fun setActiveSource(context: Context, source: String) {
        getPrefs(context).edit().putString(KEY_ACTIVE_SOURCE, source).apply()
    }

    // Last search
    fun getLastSearch(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_SEARCH, "") ?: ""
    }

    fun setLastSearch(context: Context, query: String) {
        getPrefs(context).edit().putString(KEY_LAST_SEARCH, query).apply()
    }

    fun isGelbooruConfigured(context: Context): Boolean {
        return getGelbooruUserId(context).isNotBlank() && getGelbooruApiKey(context).isNotBlank()
    }

    fun isRule34Configured(context: Context): Boolean {
        return getRule34UserId(context).isNotBlank() && getRule34ApiKey(context).isNotBlank()
    }
}
