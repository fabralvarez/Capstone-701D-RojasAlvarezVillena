package com.example.vitalarmapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.vitalarmapp.utils.local.SessionManager
import com.example.vitalarmapp.utils.local.ThemeManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class VitalarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyPreferredLocale()
        ThemeManager.initialize(this)
        val dynamicOptions = DynamicColorsOptions.Builder()
            .setPrecondition { activity, _ -> SessionManager.isMaterialYouEnabled(activity) }
            .build()

        DynamicColors.applyToActivitiesIfAvailable(this, dynamicOptions)
    }

    private fun applyPreferredLocale() {
        val localeTag = SessionManager.getAppLocale(this)
        val locales = if (localeTag.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(localeTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
