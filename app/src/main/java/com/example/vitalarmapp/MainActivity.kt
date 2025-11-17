package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import utils.NotificationHelper

private lateinit var mainLoginBtn: MaterialButton
private lateinit var mainSignupBtn: MaterialButton

class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        NotificationHelper.createNotificationChannel(this)
        darkModeChecker()
        initComponents()
        initListeners()
    }

    private fun darkModeChecker() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> Log.d(TAG, "Modo claro activo")
            Configuration.UI_MODE_NIGHT_YES -> Log.d(TAG, "Modo oscuro activo")
            else -> Log.d(TAG, "Modo de interfaz desconocido")
        }
    }

    private fun initComponents() {
        mainLoginBtn = binding.mainLoginBtn
        mainSignupBtn = binding.mainSignupBtn
    }

    private fun initListeners() {
        mainLoginBtn.setOnClickListener {
            navigateToLogin()
        }

        mainSignupBtn.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
}
