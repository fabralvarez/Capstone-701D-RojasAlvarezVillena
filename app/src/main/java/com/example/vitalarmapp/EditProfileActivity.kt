package com.example.vitalarmapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityEditProfileBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupInputListener()
        setupButtons()
        loadCurrentName()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.editProfileToolbar)
        binding.editProfileToolbar.navigationIcon = null
    }

    private fun setupInputListener() {
        updateResetButtonState()
        binding.nameEditText.doOnTextChanged { _, _, _, _ ->
            updateResetButtonState()
        }
    }

    private fun setupButtons() {
        binding.btnCancelOperation.setOnClickListener { showCancelDialog() }
        binding.btnResetName.setOnClickListener { attemptUpdateName() }
    }

    private fun loadCurrentName() {
        lifecycleScope.launch {
            val currentName = withContext(Dispatchers.IO) {
                FirebaseManager.getCurrentUserProfile()?.name
            }
            currentName?.takeIf { it.isNotBlank() }?.let { name ->
                binding.nameEditText.setText(name)
                binding.nameEditText.setSelection(name.length)
                updateResetButtonState()
            }
        }
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog
        )
            .setTitle(getString(R.string.edit_profile_cancel_title))
            .setMessage(getString(R.string.edit_profile_cancel_supporting))
            .setNegativeButton(getString(R.string.edit_profile_cancel_confirm)) { _, _ ->
                navigateBackToProfile()
            }
            .setPositiveButton(getString(R.string.edit_profile_cancel_dismiss), null)
            .show()
    }

    private fun attemptUpdateName() {
        val newName = binding.nameEditText.text?.toString()?.trim().orEmpty()
        if (newName.isEmpty()) {
            binding.nameInputLayout.error = getString(R.string.edit_profile_name_error)
            return
        }

        binding.nameInputLayout.error = null
        setLoadingState(true)

        lifecycleScope.launch {
            val isUpdated = withContext(Dispatchers.IO) {
                FirebaseManager.updateCurrentUserName(newName)
            }

            setLoadingState(false)

            if (isUpdated) {
                showSuccessDialog()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog
        )
            .setTitle(getString(R.string.edit_profile_success_title))
            .setMessage(getString(R.string.edit_profile_success_supporting))
            .setPositiveButton(getString(R.string.edit_profile_success_action)) { _, _ ->
                navigateBackToProfile()
            }
            .show()
    }

    private fun showErrorDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog
        )
            .setTitle(getString(R.string.edit_profile_error_title))
            .setMessage(getString(R.string.edit_profile_error_supporting))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun navigateBackToProfile() {
        startActivity(ProfileTabActivity.intent(this))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnResetName.isEnabled =
            !isLoading && binding.nameEditText.text?.isNotBlank() == true
        binding.btnCancelOperation.isEnabled = !isLoading
    }

    private fun updateResetButtonState() {
        binding.btnResetName.isEnabled = binding.nameEditText.text?.isNotBlank() == true
    }
}
