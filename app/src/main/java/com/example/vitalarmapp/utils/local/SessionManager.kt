package com.example.vitalarmapp.utils.local

import android.content.Context
import androidx.core.content.edit

object SessionManager {
    private const val PREFERENCES_NAME = "vitalarm_preferences"
    private const val KEY_KEEP_SESSION = "keep_session"

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
}
