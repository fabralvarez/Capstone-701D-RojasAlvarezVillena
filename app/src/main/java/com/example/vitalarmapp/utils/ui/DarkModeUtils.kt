package com.example.vitalarmapp.utils.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.Menu
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.size
import androidx.core.view.get

object DarkModeUtils {
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun applyToolbarIconColors(toolbar: MaterialToolbar, isDarkMode: Boolean? = null) {
        val tintColor = resolveTint(toolbar.context, isDarkMode)
        toolbar.navigationIcon = toolbar.navigationIcon?.mutate()?.apply { setTint(tintColor) }
        toolbar.overflowIcon = toolbar.overflowIcon?.mutate()?.apply { setTint(tintColor) }
        tintMenu(toolbar.menu, tintColor)
    }

    fun applyBottomNavigationColors(bottomNavigationView: BottomNavigationView, isDarkMode: Boolean? = null) {
        val tintColor = resolveTint(bottomNavigationView.context, isDarkMode)
        val tintList = ColorStateList.valueOf(tintColor)
        bottomNavigationView.itemIconTintList = tintList
        bottomNavigationView.itemTextColor = tintList
    }
    private fun tintMenu(menu: Menu, color: Int) {
        for (i in 0 until menu.size) {
            val icon = menu[i].icon ?: continue
            icon.mutate().setTint(color)
        }
    }

    private fun resolveTint(context: Context, isDarkMode: Boolean? = null): Int {
        val isNightMode = isDarkMode ?: isDarkMode(context)
        val tintRes = if (isNightMode) android.R.color.white else android.R.color.black
        return ContextCompat.getColor(context, tintRes)
    }
}
