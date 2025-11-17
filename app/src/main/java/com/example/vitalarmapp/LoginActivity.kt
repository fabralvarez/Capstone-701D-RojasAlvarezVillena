package com.example.vitalarmapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import utils.FirebaseManager
import utils.LoginResult

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.loginToolbar)
        binding.loginToolbar.navigationContentDescription =
            getString(R.string.login_toolbar_navigation_description)
        binding.loginToolbar.setNavigationOnClickListener {
            navigateBackToMain()
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val email = binding.emailEditText.text?.toString()?.trim().orEmpty()
        val password = binding.passwordEditText.text?.toString().orEmpty()

        var hasError = false

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.login_error_invalid_email)
            hasError = true
        } else {
            binding.emailInputLayout.error = null
        }

        when {
            password.isEmpty() -> {
                binding.passwordInputLayout.error = getString(R.string.login_error_empty_password)
                hasError = true
            }
            password.length < 6 -> {
                binding.passwordInputLayout.error = getString(R.string.login_error_short_password)
                hasError = true
            }
            else -> binding.passwordInputLayout.error = null
        }

        if (hasError) return

        lifecycleScope.launch {
            setLoadingState(true)
            when (val result = FirebaseManager.loginAndVerifyUser(email, password)) {
                LoginResult.Success -> handleLoginSuccess()
                LoginResult.UserNotFound -> showUserNotFoundDialog()
                is LoginResult.ConnectionError -> showConnectionErrorDialog(result.message)
                is LoginResult.UnknownError -> showUnknownErrorDialog(result.message)
            }
            setLoadingState(false)
        }
    }

    private fun handleLoginSuccess() {
        Toast.makeText(this, getString(R.string.login_success_toast), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LanMenuActivity::class.java))
        finish()
    }

    private fun showUserNotFoundDialog() {
        showExpressiveDialog(
            title = getString(R.string.login_dialog_user_not_found_title),
            message = getString(R.string.login_dialog_user_not_found_message)
        )
    }

    private fun showConnectionErrorDialog(message: String?) {
        val detail = if (message.isNullOrBlank()) "" else "\n\n$message"
        showExpressiveDialog(
            title = getString(R.string.login_dialog_connection_error_title),
            message = getString(R.string.login_dialog_connection_error_message) + detail
        )
    }

    private fun showUnknownErrorDialog(message: String?) {
        val detail = message ?: getString(R.string.login_dialog_unknown_error_fallback)
        showExpressiveDialog(
            title = getString(R.string.login_dialog_unknown_error_title),
            message = getString(R.string.login_dialog_unknown_error_message, detail)
        )
    }

    private fun showExpressiveDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.loginButton.text =
            if (isLoading) getString(R.string.login_loading) else getString(R.string.login_action)
    }

    private fun navigateBackToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}