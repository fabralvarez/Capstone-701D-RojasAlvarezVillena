package com.example.vitalarmapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.example.vitalarmapp.utils.local.NotificationHelper
import com.example.vitalarmapp.utils.local.SessionManager
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.ui.DarkModeUtils

private lateinit var mainLoginBtn: MaterialButton
private lateinit var mainSignupBtn: MaterialButton

class MainActivity : AppCompatActivity() {
    private companion object;

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shouldRestoreSession()) {
            navigateToHome()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        NotificationHelper.createNotificationChannel(this)
        darkModeChecker()
        initComponents()
        initListeners()
    }

    private fun darkModeChecker() {
        DarkModeUtils.applyToolbarIconColors(binding.mainToolbar)
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

    private fun shouldRestoreSession(): Boolean {
        return SessionManager.shouldKeepSession(this) && FirebaseManager.getCurrentUserId() != null
    }

    private fun navigateToHome() {
        startActivity(Intent(this, LanMenuActivity::class.java))
        finish()
    }
}
