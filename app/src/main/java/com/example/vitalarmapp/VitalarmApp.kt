package com.example.vitalarmapp

import android.app.Application
import com.example.vitalarmapp.utils.local.SessionManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class VitalarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val dynamicOptions = DynamicColorsOptions.Builder()
            .setPrecondition { activity, _ -> SessionManager.isMaterialYouEnabled(activity) }
            .build()

        DynamicColors.applyToActivitiesIfAvailable(this, dynamicOptions)
    }
}
