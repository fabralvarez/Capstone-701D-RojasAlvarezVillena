package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vitalarmapp.databinding.ActivityPassResetBinding

class PassResetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassResetBinding
    private val email: String by lazy {
        intent.getStringExtra(EXTRA_EMAIL).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassResetBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupBodyText()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.passResetToolbar)
        binding.passResetToolbar.navigationContentDescription =
            getString(R.string.login_toolbar_navigation_description)
        if (isNightModeActive()) {
            val navigationColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
            binding.passResetToolbar.navigationIcon?.setTint(navigationColor)
        }
        binding.passResetToolbar.setNavigationOnClickListener {
            navigateBackToLogin()
        }
    }

    private fun setupButtons() {
        binding.passResetFinishBtn.setOnClickListener {
            navigateBackToLogin()
        }
    }

    private fun setupBodyText() {
        val emailToShow = if (email.isNotBlank()) email else getString(R.string.email_placeholder)
        binding.passResetInfoBody.text = getString(R.string.pass_reset_info_body, emailToShow)
    }

    private fun navigateBackToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun isNightModeActive(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        const val EXTRA_EMAIL: String = "pass_reset_email"
    }
}
