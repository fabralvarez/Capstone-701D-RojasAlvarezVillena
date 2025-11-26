package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.vitalarmapp.databinding.ActivityPassRecBinding
import com.example.vitalarmapp.utils.local.OtpLocalManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PassRecActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassRecBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassRecBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupEmailWatcher()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.passRecToolbar)
        binding.passRecToolbar.navigationContentDescription =
            getString(R.string.login_toolbar_navigation_description)
        if (isNightModeActive()) {
            val navigationColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
            binding.passRecToolbar.navigationIcon?.setTint(navigationColor)
        }
        binding.passRecToolbar.setNavigationOnClickListener {
            navigateBackToLogin()
        }
    }

    private fun setupEmailWatcher() {
        binding.passRecConfirmBtn.isEnabled = false
        binding.passRecEmailTv.doOnTextChanged { text, _, _, _ ->
            val isValid = !text.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(text).matches()
            binding.passRecConfirmBtn.isEnabled = isValid
            if (isValid) {
                binding.passRecEmailInputLayout.error = null
            }
        }
    }

    private fun setupListeners() {
        binding.passRecConfirmBtn.setOnClickListener {
            sendOtpAndNavigate()
        }
    }

    private fun isNightModeActive(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun navigateBackToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun sendOtpAndNavigate() {
        val email = binding.passRecEmailTv.text?.toString()?.trim().orEmpty()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.passRecEmailInputLayout.error = getString(R.string.pass_rec_invalid_email)
            return
        }

        val wasSent = OtpLocalManager.startSession(this, email)
        if (!wasSent) {
            MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle(R.string.otp_email_intent_error_title)
                .setMessage(R.string.otp_email_intent_error_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        startActivity(Intent(this, OtpActivity::class.java))
    }
}
