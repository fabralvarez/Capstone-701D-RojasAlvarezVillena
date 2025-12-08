package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityAddMainTabBinding
import com.example.vitalarmapp.navigation.BottomNavigationHelper

class AddMainTabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMainTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMainTabBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupNavigation()
        setupActions()
        setupTopActions()
    }

    private fun setupNavigation() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.lanMenuBottomNavigation.selectedItemId = R.id.nav_add
        BottomNavigationHelper.setup(
            bottomNavigationView = binding.lanMenuBottomNavigation,
            onItemSelected = ::handleNavigation,
            onItemReselected = ::handleNavigation
        )
    }

    private fun setupTopActions() {
        binding.topAppBar.menu.clear()
        binding.topAppBar.inflateMenu(R.menu.menu_top_actions)
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.action_settings -> {
                    startActivity(SettingsActivity.intent(this))
                    true
                }

                else -> false
            }
        }
    }

    private fun setupActions() {
        binding.cardAddAlarm.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java)
            startActivity(intent)
        }

        binding.cardAddPatient.setOnClickListener {
            startActivity(AddPatsActivity.intent(this))
        }

        binding.cardAddMedication.setOnClickListener {
            startActivity(AddMedsActivity.intent(this))
        }
    }

    private fun handleNavigation(itemId: Int): Boolean = when (itemId) {
        R.id.nav_home -> {
            startActivity(Intent(this, LanMenuActivity::class.java))
            finish()
            true
        }

        R.id.nav_add -> true

        R.id.nav_profile -> {
            startActivity(ProfileTabActivity.intent(this))
            finish()
            true
        }

        else -> false
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddMainTabActivity::class.java)
    }
}
