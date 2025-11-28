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
    }

    private fun setupNavigation() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        BottomNavigationHelper.setup(binding.lanMenuBottomNavigation, this)
    }

    private fun setupActions() {
        binding.cardAddAlarm.setOnClickListener {
            startActivity(AddAlarmActivity.intent(this))
        }

        binding.cardAddPatient.setOnClickListener {
            startActivity(AddPatsActivity.intent(this))
        }

        binding.cardAddMedication.setOnClickListener {
            startActivity(AddMedsActivity.intent(this))
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddMainTabActivity::class.java)
    }
}
