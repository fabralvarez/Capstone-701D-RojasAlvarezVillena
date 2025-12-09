package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.vitalarmapp.AddAlarmActivity.Companion.EXTRA_PATIENT_ID
import com.example.vitalarmapp.AddAlarmActivity.Companion.EXTRA_PATIENT_NAME
import com.example.vitalarmapp.databinding.ActivitySelectSoundBinding
import com.example.vitalarmapp.utils.ui.DarkModeUtils
import com.google.android.material.transition.platform.MaterialSharedAxis

class SelectSoundActivity : AppCompatActivity() {
    private var isDarkMode: Boolean = false
    private lateinit var binding: ActivitySelectSoundBinding

    private var patientId: String = ""
    private var patientName: String = ""
    private var medicationName: String = ""
    private var medicationDetail: String = ""
    private var selectedSoundUri: Uri? = null
    private var selectedSoundTitle: String = ""

    private val ringtonePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            val uri = result.data?.let {
                IntentCompat.getParcelableExtra(
                    it,
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            } ?: return@registerForActivityResult

            applySelection(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        isDarkMode = DarkModeUtils.isDarkMode(this)
        binding = ActivitySelectSoundBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        patientId = intent.getStringExtra(EXTRA_PATIENT_ID).orEmpty()
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        medicationName = intent.getStringExtra(SelectMedActivity.EXTRA_MED_NAME).orEmpty()
        medicationDetail = intent.getStringExtra(SelectMedActivity.EXTRA_MED_DETAIL).orEmpty()

        setupToolbar()
        setupActions()
        applyDefaultSound()
    }

    private fun setupToolbar() {
        binding.selectSoundToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupActions() {
        binding.selectSoundPickerButton.setOnClickListener { launchSoundPicker() }
        binding.selectSoundContinue.setOnClickListener {
            val soundUri = selectedSoundUri?.toString().orEmpty()
            startActivity(
                ConfirmAlarmActivity.intent(
                    context = this,
                    patientId = patientId,
                    patientName = patientName,
                    medicationName = medicationName,
                    medicationDetail = medicationDetail,
                    soundUri = soundUri,
                    soundTitle = selectedSoundTitle
                )
            )
        }
    }

    private fun applyDefaultSound() {
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        defaultUri?.let { applySelection(it) }
    }

    private fun launchSoundPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        }
        ringtonePickerLauncher.launch(intent)
    }

    private fun applySelection(uri: Uri) {
        selectedSoundUri = uri
        selectedSoundTitle = RingtoneManager.getRingtone(this, uri)
            ?.getTitle(this)
            ?.takeIf { it.isNotBlank() }
            ?: getString(R.string.select_sound_default_label)

        binding.selectSoundValue.text = selectedSoundTitle
        binding.selectSoundContinue.isEnabled = true
    }

    companion object {
        fun intent(
            context: Context,
            patientId: String,
            patientName: String,
            medicationName: String,
            medicationDetail: String,
        ): Intent = Intent(context, SelectSoundActivity::class.java).apply {
            putExtra(EXTRA_PATIENT_ID, patientId)
            putExtra(EXTRA_PATIENT_NAME, patientName)
            putExtra(SelectMedActivity.EXTRA_MED_NAME, medicationName)
            putExtra(SelectMedActivity.EXTRA_MED_DETAIL, medicationDetail)
        }
    }
}
