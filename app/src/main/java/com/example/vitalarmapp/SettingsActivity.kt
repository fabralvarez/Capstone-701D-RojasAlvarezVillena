package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivitySettingsBinding
import com.example.vitalarmapp.utils.local.SessionManager
import com.example.vitalarmapp.utils.local.ThemeManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        setupLanguagePreference()
        handleShortcutAction()
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
                .show()
            ThemeManager.notifyThemeChanged(this)
        }

        binding.layoutClearCache.setOnClickListener {
            clearCache()
        }
    }

    private fun setupLanguagePreference() {
        updateLanguageCopy()
        binding.layoutLanguage.setOnClickListener { showLanguageDialog() }
    }

    private fun showLanguageDialog() {
        val options = languageOptions
        val labels = options.map { getString(it.labelRes) }.toTypedArray()
        val currentTag = currentLocaleTag()
        val selectedIndex = options.indexOfFirst { it.tag.equals(currentTag, ignoreCase = true) }
            .takeIf { it >= 0 } ?: 0

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(labels, selectedIndex) { dialog, which ->
                val option = options[which]
                val localeList = LocaleListCompat.forLanguageTags(option.tag)
                AppCompatDelegate.setApplicationLocales(localeList)
                SessionManager.setAppLocale(this, option.tag)
                val message = getString(R.string.settings_language_changed, labels[which])
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                updateLanguageCopy()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun updateLanguageCopy() {
        val currentTag = currentLocaleTag()
        val current = languageOptions.firstOrNull { it.tag.equals(currentTag, ignoreCase = true) }
            ?: languageOptions.first()
        binding.tvLanguageTitle.text = getString(R.string.settings_language_title)
        binding.tvLanguageSubtitle.text = getString(current.labelRes)
    }

    private fun currentLocaleTag(): String {
        val saved = SessionManager.getAppLocale(this)
        if (!saved.isNullOrBlank()) return saved
        val appLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (appLocales.isNotBlank()) return appLocales
        return languageOptions.first().tag
    }

    private fun handleShortcutAction() {
        if (intent?.action == ACTION_CLEAR_CACHE) {
            clearCache()
        }
    }

    private fun clearCache() {
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
                .show()
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

    private data class LocaleOption(val tag: String, val labelRes: Int)

    private val languageOptions = listOf(
        LocaleOption("es-US", R.string.language_option_spanish_us),
        LocaleOption("en-US", R.string.language_option_english_us)
    )

    companion object {
        const val ACTION_CLEAR_CACHE = "com.example.vitalarmapp.action.CLEAR_CACHE"

        fun intent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
