package com.example.vitalarmapp

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityAlarmRingingBinding
import com.example.vitalarmapp.models.AlarmRecord
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.AlarmScheduler.Companion.extractAlarmPayload
import com.example.vitalarmapp.utils.local.AlarmScheduler.Companion.putAlarmPayload
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFadeThrough
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class AlarmRingingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingingBinding
    private var record: AlarmRecord? = null
    private var ringtone: Ringtone? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraInternal()
            } else {
                Snackbar.make(binding.root, R.string.alarm_ring_capture_failed, Snackbar.LENGTH_LONG).show()
            }
        }

    private val captureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            record?.let { markVerified(it.id) }
            Snackbar.make(binding.root, R.string.alarm_ring_verified, Snackbar.LENGTH_LONG).show()
            finish()
        } else {
            Snackbar.make(binding.root, R.string.alarm_ring_capture_failed, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialFadeThrough()
        window.returnTransition = MaterialFadeThrough()
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        applySurfacePalette()
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        makeFullScreen()
        onBackPressedDispatcher.addCallback(this) { }
        loadRecord()
        bindContent()
        setupActions()
        startAlarmSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }

    private fun loadRecord() {
        record = intent.extractAlarmPayload()
        if (record == null) {
            Snackbar.make(binding.root, R.string.alarm_ring_capture_failed, Snackbar.LENGTH_LONG).show()
            finish()
        } else {
            markTriggered()
        }
    }

    private fun bindContent() {
        val item = record ?: return
        binding.alarmRingPatient.text = item.patientName
        binding.alarmRingMedication.text = getString(
            R.string.alarm_ring_medication,
            item.medicationName,
            item.medicationDetail
        )
        binding.alarmRingTime.text = getString(R.string.alarm_ring_time, item.date, item.time)
    }

    private fun setupActions() {
        binding.alarmRingVerify.setOnClickListener { launchCamera() }
    }

    private fun markTriggered() {
        val alarmId = record?.id ?: return
        lifecycleScope.launch {
            FirebaseManager.markAlarmTriggered(alarmId)
        }
    }

    private fun launchCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }
        launchCameraInternal()
    }

    private fun launchCameraInternal() {
        captureLauncher.launch(null)
    }

    private fun markVerified(id: String) {
        lifecycleScope.launch {
            FirebaseManager.markAlarmVerified(id)
        }
    }

    private fun startAlarmSound() {
        val preferredUri = record?.soundUri
            ?.takeIf { it.isNotBlank() }?.toUri()

        val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val uri = preferredUri ?: fallbackUri
        ringtone = RingtoneManager.getRingtone(this, uri)
        ringtone?.play()
    }

    private fun applySurfacePalette() {
        val surfaceColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorSurface,
            resources.getColor(android.R.color.background_dark, theme)
        )
        binding.root.setBackgroundColor(surfaceColor)
        val controller = WindowInsetsControllerCompat(window, binding.root)
        val lightBars = MaterialColors.isColorLight(surfaceColor)
        controller.isAppearanceLightStatusBars = lightBars
        controller.isAppearanceLightNavigationBars = lightBars
    }

    private fun makeFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    companion object {
        fun intent(context: android.content.Context, record: AlarmRecord) =
            android.content.Intent(context, AlarmRingingActivity::class.java).apply {
                putAlarmPayload(record)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
    }
}
