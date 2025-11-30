package com.example.vitalarmapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.vitalarmapp.utils.local.SessionManager
import com.google.android.material.color.DynamicColors

class VitalarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (SessionManager.isMaterialYouEnabled(activity)) {
                    DynamicColors.applyToActivityIfAvailable(activity)
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
