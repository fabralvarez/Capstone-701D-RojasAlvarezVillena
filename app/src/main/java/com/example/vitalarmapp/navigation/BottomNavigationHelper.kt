package com.example.vitalarmapp.navigation

import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {
    fun setup(
        bottomNavigationView: BottomNavigationView,
        onItemSelected: (Int) -> Boolean,
        onItemReselected: ((Int) -> Unit)? = null
    ) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            onItemSelected(item.itemId)
        }
        bottomNavigationView.setOnItemReselectedListener { item ->
            onItemReselected?.invoke(item.itemId)
        }
    }
}
