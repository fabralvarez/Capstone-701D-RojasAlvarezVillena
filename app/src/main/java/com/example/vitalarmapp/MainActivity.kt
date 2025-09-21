package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityMainBinding
import com.example.vitalarmapp.utils.PreferencesManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        preferencesManager = PreferencesManager(this)
        // Si ya está logueado, ir directamente al menú
        if (preferencesManager.isLoggedIn()) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }
        darkModeChecker()
        initListeners()
    }

    // Verificar el modo oscuro
    private fun darkModeChecker() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_YES -> {}
        }
    }

    private fun initListeners() {
        binding.btnIngresar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegistrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}