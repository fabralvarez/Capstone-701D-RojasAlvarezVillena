package com.example.vitalarmapp.utils.local

import android.app.Activity
import android.app.Application
import android.os.Bundle

object ThemeManager : Application.ActivityLifecycleCallbacks {
    private val activeActivities = mutableSetOf<Activity>()
    private var lastMaterialYouState: Boolean? = null

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        lastMaterialYouState = SessionManager.isMaterialYouEnabled(application)
    }

    fun notifyThemeChanged(context: Activity) {
        val currentState = SessionManager.isMaterialYouEnabled(context)
        if (currentState == lastMaterialYouState) return

        lastMaterialYouState = currentState
        activeActivities.toList().forEach { activity ->
            if (!activity.isFinishing && !activity.isDestroyed) {
                activity.recreate()
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activeActivities.add(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        activeActivities.remove(activity)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}
