package com.example.vitalarmapp.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.AddMainTabActivity
import com.example.vitalarmapp.LanMenuActivity
import com.example.vitalarmapp.ProfileTabActivity
import com.example.vitalarmapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {
    fun setup(
        bottomNavigationView: BottomNavigationView,
        activity: AppCompatActivity,
        onHomeSelected: (() -> Unit)? = null
    ) {
        bottomNavigationView.apply {
            selectedItemId = selectedItemFor(activity)
            setOnItemSelectedListener { item ->
                handleNavigation(item.itemId, activity, onHomeSelected)
            }
            setOnItemReselectedListener { item ->
                handleNavigation(item.itemId, activity, onHomeSelected)
            }
        }
    }

    private fun handleNavigation(
        itemId: Int,
        activity: AppCompatActivity,
        onHomeSelected: (() -> Unit)?
    ): Boolean {
        val destination = when (itemId) {
            R.id.nav_home -> LanMenuActivity::class.java
            R.id.nav_add -> AddMainTabActivity::class.java
            R.id.nav_profile -> ProfileTabActivity::class.java
            else -> null
        }

        if (itemId == R.id.nav_home && activity is LanMenuActivity) {
            onHomeSelected?.invoke()
        }

        destination?.let {
            if (activity::class.java != it) {
                activity.startActivity(Intent(activity, it))
            }
            return true
        }
        return itemId == R.id.nav_home
    }

    private fun selectedItemFor(activity: AppCompatActivity): Int = when (activity) {
        is LanMenuActivity -> R.id.nav_home
        is AddMainTabActivity -> R.id.nav_add
        is ProfileTabActivity -> R.id.nav_profile
        else -> R.id.nav_home
    }
}
