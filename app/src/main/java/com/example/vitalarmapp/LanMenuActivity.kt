package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityLanMenuBinding
import com.example.vitalarmapp.navigation.BottomNavigationHelper
import com.example.vitalarmapp.ui.add.AddMainTabFragment
import com.example.vitalarmapp.ui.home.HomeTabFragment
import com.example.vitalarmapp.ui.profile.ProfileTabFragment
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanMenuBinding
    private var currentTabId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanMenuBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupBottomNavigation()
        setupAppBar()

        val initialTab = savedInstanceState?.getInt(SELECTED_TAB_KEY) ?: R.id.nav_home
        currentTabId = initialTab
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

    private fun setupAppBar() {
        binding.lanMenuToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    startActivity(SettingsActivity.intent(this))
                    true
                }

                else -> false
            }
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
        currentTabId = itemId
        updateAppBarVisibility(itemId)
        val fragment = showFragment(itemId) ?: return false
        if (itemId == R.id.nav_home) {
            loadToolbarGreeting()
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
        val transaction = fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.m3_fade_through_enter,
                R.animator.m3_fade_through_exit,
                R.animator.m3_fade_through_enter,
                R.animator.m3_fade_through_exit
            )
            .setReorderingAllowed(true)

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

    private fun updateAppBarVisibility(itemId: Int) {
        binding.lanMenuAppBar.isVisible = itemId == R.id.nav_home
    }

    private fun loadToolbarGreeting() {
        lifecycleScope.launch {
            val userName = withContext(Dispatchers.IO) {
                FirebaseManager.getCurrentUserName()
            }
            binding.lanMenuToolbar.title = getString(R.string.home_greeting, userName)
        }
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
