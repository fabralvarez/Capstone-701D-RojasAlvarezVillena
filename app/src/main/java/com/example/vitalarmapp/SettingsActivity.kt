package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivitySettingsBinding
import com.example.vitalarmapp.utils.local.SessionManager
import com.example.vitalarmapp.utils.local.ThemeManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupAppBar()
        setupSwitches()
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.topAppBar.menu.clear()
        binding.topAppBar.inflateMenu(R.menu.menu_settings_actions)
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> {
                    startActivity(Intent(this, AbtAppActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun setupSwitches() {
        binding.switchMaterialYou.isChecked = SessionManager.isMaterialYouEnabled(this)
        updateMaterialYouCopy(binding.switchMaterialYou.isChecked)
        binding.switchMaterialYou.setOnCheckedChangeListener { _, isChecked ->
            SessionManager.setMaterialYouEnabled(this, isChecked)
            updateMaterialYouCopy(isChecked)
            val message = if (isChecked) {
                getString(R.string.settings_material_you_enabled)
            } else {
                getString(R.string.settings_material_you_disabled)
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.switchMaterialYou)
                .show()
            ThemeManager.notifyThemeChanged(this)
        }

        binding.layoutClearCache.setOnClickListener {
            lifecycleScope.launch {
                val cleaned = withContext(Dispatchers.IO) {
                    SessionManager.clearAppCache(this@SettingsActivity)
                }
                val message = if (cleaned) {
                    R.string.settings_cache_cleared
                } else {
                    R.string.settings_cache_clear_failed
                }
                Snackbar.make(binding.root, getString(message), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.layoutClearCache)
                    .show()
            }
        }
    }

    private fun updateMaterialYouCopy(isEnabled: Boolean) {
        if (isEnabled) {
            binding.tvMaterialYouTitle.text = getString(R.string.settings_material_you_title_default)
            binding.tvMaterialYouSubtitle.text =
                getString(R.string.settings_material_you_default_subtitle)
        } else {
            binding.tvMaterialYouTitle.text = getString(R.string.settings_material_you_title)
            binding.tvMaterialYouSubtitle.text = getString(R.string.settings_material_you_subtitle)
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
