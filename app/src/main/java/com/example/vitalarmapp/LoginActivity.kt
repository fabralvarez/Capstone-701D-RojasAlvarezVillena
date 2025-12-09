package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.autofill.AutofillManager
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.exceptions.CreateCredentialException
import androidx.fragment.app.commit
import com.example.vitalarmapp.databinding.ActivityLoginBinding
import com.example.vitalarmapp.databinding.LoginBottomSheetBinding
import com.example.vitalarmapp.ui.LoadingIndicatorFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import com.example.vitalarmapp.utils.local.ErrorMessageTranslator
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.firebase.LoginResult
import com.example.vitalarmapp.utils.local.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val autofillManager: AutofillManager? by lazy { getSystemService(AutofillManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupListeners()
        requestAutofillSupport()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.loginToolbar)
        binding.loginToolbar.navigationContentDescription =
            getString(R.string.login_toolbar_navigation_description)
        if (isNightModeActive()) {
            val navigationColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
            binding.loginToolbar.navigationIcon?.setTint(navigationColor)
        }
        binding.loginToolbar.setNavigationOnClickListener {
            navigateBackToMain()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_login_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_more_options -> {
            showOverflowMenu()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun isNightModeActive(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupListeners() {
        binding.loginLoginBtn.setOnClickListener {
            attemptLogin()
        }

        binding.loginForgotPassBtn.setOnClickListener {
            startActivity(Intent(this, PassRecActivity::class.java))
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
                LoginResult.Success -> handleLoginSuccess(email, password)
                LoginResult.UserNotFound -> showUserNotFoundDialog()
                is LoginResult.ConnectionError -> showConnectionErrorDialog(result.message)
                is LoginResult.UnknownError -> showUnknownErrorDialog(result.message)
            }
            setLoadingState(false)
        }
    }

    private fun handleLoginSuccess(email: String, password: String) {
        showSaveSessionBottomSheet(email, password)
    }

    private fun showSaveSessionBottomSheet(email: String, password: String) {
        val bottomSheetDialog = BottomSheetDialog(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog
        )
        val bottomSheetBinding = LoginBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.isDraggable = false

        bottomSheetBinding.loginBottomSheetDismissBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
            SessionManager.setKeepSession(this, false)
            promptToSavePassword(email, password)
        }

        bottomSheetBinding.loginBottomSheetAgreeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
            SessionManager.setKeepSession(this, true)
            promptToSavePassword(email, password)
        }

        bottomSheetDialog.show()
    }

    private fun promptToSavePassword(email: String, password: String) {
        lifecycleScope.launch {
            val createPasswordRequest = CreatePasswordRequest(id = email, password = password)
            try {
                credentialManager.createCredential(this@LoginActivity, createPasswordRequest)
            } catch (_: CreateCredentialException) {
                // Ignored: the user may decline saving credentials or the operation may fail silently.
            }
            navigateToHome()
        }
    }

    private fun showUserNotFoundDialog() {
        showExpressiveDialog(
            title = getString(R.string.login_dialog_user_not_found_title),
            message = getString(R.string.login_dialog_user_not_found_message)
        )
    }

    private fun showConnectionErrorDialog(message: String?) {
        val detail = message?.let { ErrorMessageTranslator.toSpanish(this, it) }
            ?: getString(R.string.error_detail_network)
        val composedMessage =
            getString(R.string.login_dialog_connection_error_message) + "\n\n" + detail
        showExpressiveDialog(
            title = getString(R.string.login_dialog_connection_error_title),
            message = composedMessage
        )
    }

    private fun showUnknownErrorDialog(message: String?) {
        val detail = message?.let { ErrorMessageTranslator.toSpanish(this, it) }
            ?: getString(R.string.login_dialog_unknown_error_fallback)
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
        binding.loginLoginBtn.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
        binding.loginForgotPassBtn.isEnabled = !isLoading
        binding.loginLoginBtn.text =
            if (isLoading) getString(R.string.login_loading) else getString(R.string.login_action)

        if (isLoading) {
            showLoadingOverlay()
        } else {
            hideLoadingOverlay()
        }
    }

    private fun requestAutofillSupport() {
        autofillManager?.requestAutofill(binding.emailEditText)
        autofillManager?.requestAutofill(binding.passwordEditText)
    }

    private fun navigateBackToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun showOverflowMenu() {
        val anchor = binding.loginToolbar.findViewById<View>(R.id.action_more_options)
            ?: run {
                startActivity(SettingsActivity.intent(this))
                return
            }
        val popupContext = ContextThemeWrapper(this, R.style.ThemeOverlay_Vitalarm_PopupMenu)
        PopupMenu(popupContext, anchor).apply {
            menuInflater.inflate(R.menu.menu_login_overflow, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_open_settings -> {
                        startActivity(SettingsActivity.intent(this@LoginActivity))
                        true
                    }

                    else -> false
                }
            }
            setForceShowIcon(true)
            show()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, LanMenuActivity::class.java))
        finish()
    }

    private fun showLoadingOverlay() {
        binding.loginLoadingContainer.isVisible = true
        if (supportFragmentManager.findFragmentByTag(LoadingIndicatorFragment.TAG) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(
                    binding.loginLoadingContainer.id,
                    LoadingIndicatorFragment(),
                    LoadingIndicatorFragment.TAG
                )
            }
        }
    }

    private fun hideLoadingOverlay() {
        binding.loginLoadingContainer.isVisible = false
        supportFragmentManager.findFragmentByTag(LoadingIndicatorFragment.TAG)?.let { fragment ->
            supportFragmentManager.commit {
                remove(fragment)
            }
        }
    }
}