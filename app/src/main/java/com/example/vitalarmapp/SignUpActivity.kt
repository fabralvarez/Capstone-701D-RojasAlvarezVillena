package com.example.vitalarmapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivitySignUpBinding
import com.example.vitalarmapp.util.ErrorMessageTranslator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import models.User
import utils.FirebaseManager
import utils.RegistrationResult

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        initListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.signupToolbar)
        binding.signupToolbar.setNavigationOnClickListener {
            navigateToMain()
        }
    }

    private fun initListeners() {
        binding.signupRegisterBtn.setOnClickListener {
            attemptRegisterUser()
        }
    }

    private fun attemptRegisterUser() {
        val name = binding.signupNameTf.text?.toString()?.trim().orEmpty()
        val email = binding.signupEmailTf.text?.toString()?.trim().orEmpty()
        val password = binding.signupPassTf.text?.toString().orEmpty()
        val confirmPassword = binding.signupConfirmPassTf.text?.toString().orEmpty()

        var hasError = false

        if (name.isEmpty()) {
            binding.signupNameInputLayout.error = getString(R.string.sign_up_error_empty_name)
            hasError = true
        } else {
            binding.signupNameInputLayout.error = null
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmailInputLayout.error = getString(R.string.sign_up_error_invalid_email)
            hasError = true
        } else {
            binding.signupEmailInputLayout.error = null
        }

        when {
            password.isEmpty() -> {
                binding.signupPassInputLayout.error = getString(R.string.sign_up_error_empty_password)
                hasError = true
            }

            password.length < 6 -> {
                binding.signupPassInputLayout.error = getString(R.string.sign_up_error_password_length)
                hasError = true
            }

            else -> binding.signupPassInputLayout.error = null
        }

        when {
            confirmPassword.isEmpty() -> {
                binding.signupConfirmPassInputLayout.error = getString(R.string.sign_up_error_empty_confirm_password)
                hasError = true
            }

            password != confirmPassword -> {
                binding.signupConfirmPassInputLayout.error = getString(R.string.sign_up_error_password_mismatch)
                hasError = true
            }

            else -> binding.signupConfirmPassInputLayout.error = null
        }

        if (hasError) return

        lifecycleScope.launch {
            setLoadingState(true)
            when (val result = FirebaseManager.registerUser(name, email, password)) {
                is RegistrationResult.Success -> {
                    FirebaseManager.logout()
                    showSignUpSuccessDialog(result.user)
                }

                RegistrationResult.EmailAlreadyInUse -> {
                    showSignUpErrorDialog(getString(R.string.sign_up_error_message_email_in_use))
                }

                RegistrationResult.WeakPassword -> {
                    showSignUpErrorDialog(getString(R.string.sign_up_error_message_weak_password))
                }

                is RegistrationResult.ConnectionError -> {
                    val detail = result.message?.let { ErrorMessageTranslator.toSpanish(this@SignUpActivity, it) }
                        ?: getString(R.string.error_detail_network)
                    showSignUpErrorDialog(detail)
                }

                is RegistrationResult.UnknownError -> {
                    val detail = result.message?.let { ErrorMessageTranslator.toSpanish(this@SignUpActivity, it) }
                        ?: getString(R.string.sign_up_error_message_generic)
                    showSignUpErrorDialog(detail)
                }
            }
            setLoadingState(false)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.signupRegisterBtn.isEnabled = !isLoading
        binding.signupRegisterBtn.text =
            if (isLoading) getString(R.string.sign_up_register_loading) else getString(R.string.register)
    }

    private fun showSignUpSuccessDialog(user: User) {
        val userName = if (user.name.isNotBlank()) user.name else getString(R.string.sign_up_success_fallback_name)
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle(getString(R.string.sign_up_success_dialog_title))
            .setMessage(getString(R.string.sign_up_success_dialog_supporting, userName))
            .setPositiveButton(getString(R.string.sign_up_success_primary_action)) { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton(getString(R.string.sign_up_success_secondary_action)) { _, _ ->
                navigateToMain()
            }
            .show()
    }

    private fun showSignUpErrorDialog(detailMessage: String) {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle(getString(R.string.sign_up_error_dialog_title))
            .setMessage(detailMessage)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}