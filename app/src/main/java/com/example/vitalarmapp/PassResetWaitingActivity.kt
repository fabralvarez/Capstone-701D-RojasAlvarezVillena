package com.example.vitalarmapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vitalarmapp.databinding.ActivityPassResetWaitingBinding
import androidx.core.net.toUri

class PassResetWaitingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassResetWaitingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassResetWaitingBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupToolbar()
        setupContent()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.passWaitToolbar)
        binding.passWaitToolbar.navigationContentDescription =
            getString(R.string.login_toolbar_navigation_description)
        if (isNightModeActive()) {
            val navigationColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
            binding.passWaitToolbar.navigationIcon?.setTint(navigationColor)
        }
        binding.passWaitToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupContent() {
        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()
        val emailToShow = email.ifBlank { getString(R.string.email_placeholder) }
        binding.passWaitInfoBody.text = getString(R.string.pass_wait_info_body, emailToShow)
        binding.passWaitInfoTitle.text = getString(R.string.pass_wait_info_title)
    }

    private fun setupButtons() {
        binding.passWaitOpenEmailBtn.setOnClickListener {
            openEmailApp()
        }
        binding.passWaitContinueBtn.setOnClickListener {
            val email = intent.getStringExtra(EXTRA_EMAIL)
            val intent = Intent(this, PassResetActivity::class.java)
            intent.putExtra(PassResetActivity.EXTRA_EMAIL, email)
            startActivity(intent)
        }
    }

    private fun openEmailApp() {
        val emailIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_EMAIL)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(emailIntent)
        } catch (_: ActivityNotFoundException) {
            val mailtoIntent = Intent(Intent.ACTION_VIEW, "mailto:".toUri())
            startActivity(mailtoIntent)
        }
    }

    private fun isNightModeActive(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        const val EXTRA_EMAIL: String = "pass_wait_email"
    }
}
