package com.example.vitalarmapp.utils.local

import android.content.Context
import androidx.core.content.edit

object SessionManager {
    private const val PREFERENCES_NAME = "vitalarm_preferences"
    private const val KEY_KEEP_SESSION = "keep_session"
    private const val KEY_MATERIAL_YOU = "material_you_enabled"
    private const val KEY_AUTO_CLEAR_CACHE = "auto_clear_cache_enabled"
    private const val KEY_APP_LOCALE = "app_locale"

    fun setKeepSession(context: Context, keepSession: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit { putBoolean(KEY_KEEP_SESSION, keepSession) }
    }

    fun shouldKeepSession(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_KEEP_SESSION, false)
    }

    fun setMaterialYouEnabled(context: Context, isEnabled: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit { putBoolean(KEY_MATERIAL_YOU, isEnabled) }
    }

    fun isMaterialYouEnabled(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_MATERIAL_YOU, false)
    }

    fun setAutoClearCacheEnabled(context: Context, isEnabled: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit { putBoolean(KEY_AUTO_CLEAR_CACHE, isEnabled) }
    }

    fun isAutoClearCacheEnabled(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_AUTO_CLEAR_CACHE, false)
    }

    fun setAppLocale(context: Context, localeTag: String?) {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            if (localeTag.isNullOrBlank()) {
                remove(KEY_APP_LOCALE)
            } else {
                putString(KEY_APP_LOCALE, localeTag)
            }
        }
    }

    fun getAppLocale(context: Context): String? {
        val sharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_APP_LOCALE, null)
    }

    fun clearAppCache(context: Context): Boolean {
        return try {
            context.cacheDir?.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
            true
        } catch (_: Exception) {
            false
        }
    }
}
