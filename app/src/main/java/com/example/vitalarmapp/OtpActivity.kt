package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vitalarmapp.databinding.ActivityOtpBinding
import com.example.vitalarmapp.utils.local.OtpLocalManager
import com.example.vitalarmapp.utils.local.OtpVerificationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private var resendTimer: CountDownTimer? = null
    private var keepOtpSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupInput()
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
        if (!isChangingConfigurations && !keepOtpSession) {
            OtpLocalManager.clearSession()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.otpToolbar)
        binding.otpToolbar.navigationContentDescription =
            getString(R.string.login_toolbar_navigation_description)
        if (isNightModeActive()) {
            val navigationColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
            binding.otpToolbar.navigationIcon?.setTint(navigationColor)
        }
        binding.otpToolbar.setNavigationOnClickListener {
            OtpLocalManager.clearSession()
            navigateBackToPassRec()
        }
    }

    private fun setupInput() {
        binding.otpInputLayout.setEndIconOnClickListener {
            binding.otpCodeTv.text?.clear()
        }
    }

    private fun setupButtons() {
        binding.otpConfirmBtn.setOnClickListener { verifyOtpCode() }
        binding.otpResendBtn.setOnClickListener { resendOtp() }
    }

    private fun verifyOtpCode() {
        val code = binding.otpCodeTv.text?.toString()?.trim().orEmpty()
        if (code.length != 6) {
            binding.otpInputLayout.error = getString(R.string.otp_code_length_error)
            return
        } else {
            binding.otpInputLayout.error = null
        }

        when (OtpLocalManager.verifyOtp(code)) {
            OtpVerificationResult.Success -> navigateToPassReset()
            OtpVerificationResult.Invalid -> showDialog(
                getString(R.string.otp_invalid_title),
                getString(R.string.otp_invalid_message)
            )
            OtpVerificationResult.Expired -> showDialog(
                getString(R.string.otp_expired_title),
                getString(R.string.otp_expired_message)
            )
            OtpVerificationResult.Missing -> showDialog(
                getString(R.string.otp_missing_title),
                getString(R.string.otp_missing_message)
            )
        }
    }

    private fun resendOtp() {
        val wasSent = OtpLocalManager.sendNewOtp(this)
        if (!wasSent) {
            showDialog(
                getString(R.string.otp_email_intent_error_title),
                getString(R.string.otp_email_intent_error_message)
            )
            return
        }
        startResendCountdown()
    }

    private fun startResendCountdown() {
        binding.otpResendBtn.isEnabled = false
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = ((millisUntilFinished + 999) / 1000).toInt()
                binding.otpResendBtn.text = getString(R.string.otp_resend_countdown, secondsLeft)
            }

            override fun onFinish() {
                binding.otpResendBtn.isEnabled = true
                binding.otpResendBtn.text = getString(R.string.otp_resend_button)
            }
        }.start()
    }

    private fun navigateBackToPassRec() {
        val intent = Intent(this, PassRecActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun navigateToPassReset() {
        keepOtpSession = true
        startActivity(Intent(this, PassResetActivity::class.java))
        finish()
    }

    private fun showDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun isNightModeActive(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }
}
