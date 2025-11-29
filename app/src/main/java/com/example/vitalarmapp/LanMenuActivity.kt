package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vitalarmapp.databinding.ActivityLanMenuBinding
import com.example.vitalarmapp.navigation.BottomNavigationHelper
import com.example.vitalarmapp.ui.add.AddMainTabFragment
import com.example.vitalarmapp.ui.home.HomeTabFragment
import com.example.vitalarmapp.ui.profile.ProfileTabFragment

class LanMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanMenuBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupBottomNavigation()

        val initialTab = savedInstanceState?.getInt(SELECTED_TAB_KEY) ?: R.id.nav_home
        binding.lanMenuBottomNavigation.selectedItemId = initialTab
        switchToTab(initialTab)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_KEY, binding.lanMenuBottomNavigation.selectedItemId)
    }

    fun selectTab(itemId: Int) {
        if (binding.lanMenuBottomNavigation.selectedItemId == itemId) {
            handleTabReselected(itemId)
        } else {
            binding.lanMenuBottomNavigation.selectedItemId = itemId
        }
    }

    private fun setupBottomNavigation() {
        BottomNavigationHelper.setup(
            bottomNavigationView = binding.lanMenuBottomNavigation,
            onItemSelected = { itemId -> switchToTab(itemId) },
            onItemReselected = { itemId -> handleTabReselected(itemId) }
        )
    }

    private fun switchToTab(itemId: Int): Boolean {
        val fragment = showFragment(itemId) ?: return false
        if (itemId == R.id.nav_home) {
            (fragment as? HomeTabFragment)?.refreshContent()
        }
        return true
    }

    private fun handleTabReselected(itemId: Int) {
        if (itemId == R.id.nav_home) {
            (supportFragmentManager.findFragmentByTag(fragmentTag(itemId)) as? HomeTabFragment)
                ?.refreshContent()
        }
    }

    private fun showFragment(itemId: Int): Fragment? {
        val tag = fragmentTag(itemId) ?: return null
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().setReorderingAllowed(true)

        fragmentManager.fragments.forEach { transaction.hide(it) }

        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = when (itemId) {
                R.id.nav_home -> HomeTabFragment()
                R.id.nav_add -> AddMainTabFragment()
                R.id.nav_profile -> ProfileTabFragment()
                else -> null
            }
            fragment?.let { transaction.add(R.id.fragmentContainer, it, tag) }
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
        return fragment
    }

    private fun fragmentTag(itemId: Int): String? = when (itemId) {
        R.id.nav_home -> TAG_HOME
        R.id.nav_add -> TAG_ADD
        R.id.nav_profile -> TAG_PROFILE
        else -> null
    }

    companion object {
        private const val SELECTED_TAB_KEY = "selected_tab"
        private const val TAG_HOME = "home_tab"
        private const val TAG_ADD = "add_tab"
        private const val TAG_PROFILE = "profile_tab"
    }
}
