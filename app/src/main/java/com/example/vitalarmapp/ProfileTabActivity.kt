package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityProfileTabBinding
import com.example.vitalarmapp.navigation.BottomNavigationHelper
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileTabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileTabBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupNavigation()
        setupActions()
        loadProfile()
        setupTopActions()
    }

    private fun setupNavigation() {
        binding.lanMenuBottomNavigation.selectedItemId = R.id.nav_profile
        BottomNavigationHelper.setup(
            bottomNavigationView = binding.lanMenuBottomNavigation,
            onItemSelected = ::handleNavigation,
            onItemReselected = { itemId -> handleNavigation(itemId) }
        )
    }

    private fun setupActions() {
        binding.btnProfileLogout.setOnClickListener { logoutUser() }
    }

    private fun setupTopActions() {
        binding.topAppBar.menu.clear()
        binding.topAppBar.inflateMenu(R.menu.menu_top_actions)
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    startActivity(SettingsActivity.intent(this))
                    true
                }

                else -> false
            }
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            val name = withContext(Dispatchers.IO) { FirebaseManager.getCurrentUserName() }
            val email = FirebaseAuth.getInstance().currentUser?.email
                ?: getString(R.string.profile_unknown_email)

            binding.tvProfileName.text = getString(R.string.profile_greeting_format, name)
            binding.tvProfileEmail.text = email
        }
    }

    private fun logoutUser() {
        FirebaseManager.logout()
        SessionManager.setKeepSession(this, false)
        Toast.makeText(this, getString(R.string.profile_logout_message), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun handleNavigation(itemId: Int): Boolean = when (itemId) {
        R.id.nav_home -> {
            startActivity(Intent(this, LanMenuActivity::class.java))
            finish()
            true
        }

        R.id.nav_add -> {
            startActivity(AddMainTabActivity.intent(this))
            finish()
            true
        }

        R.id.nav_profile -> true

        else -> false
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, ProfileTabActivity::class.java)
    }
}
