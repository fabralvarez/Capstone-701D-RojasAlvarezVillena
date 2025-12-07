package com.example.vitalarmapp

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.vitalarmapp.databinding.ActivityAlarmRingingBinding
import com.example.vitalarmapp.utils.local.AlarmRecord
import com.example.vitalarmapp.utils.local.AlarmRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.FileProvider
import com.example.vitalarmapp.utils.local.AlarmScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFadeThrough

class AlarmRingingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingingBinding
    private val repository by lazy { AlarmRepository(this) }
    private var record: AlarmRecord? = null
    private var ringtone: Ringtone? = null
    private var pendingPhotoUri: Uri? = null
    private var pendingPhotoFile: File? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraInternal()
            } else {
                Snackbar.make(binding.root, R.string.alarm_ring_capture_failed, Snackbar.LENGTH_LONG).show()
            }
        }

    private val captureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val photoPath = pendingPhotoFile?.absolutePath ?: return@registerForActivityResult
            record?.let { repository.markVerified(it.id, photoPath) }
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
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID).orEmpty()
        record = repository.get(alarmId)
        if (record == null) {
            Snackbar.make(binding.root, R.string.alarm_ring_capture_failed, Snackbar.LENGTH_LONG).show()
            finish()
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
        binding.alarmRingTime.text = item.time
    }

    private fun setupActions() {
        binding.alarmRingVerify.setOnClickListener { launchCamera() }
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
        val item = record ?: return
        val photosDir = File(filesDir, "alarm_photos").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(photosDir, "${item.id}_$stamp.jpg")
        pendingPhotoFile = file
        pendingPhotoUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )
        captureLauncher.launch(pendingPhotoUri)
    }

    private fun startAlarmSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, uri)
        ringtone?.play()
    }

    private fun makeFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _: View, insets: WindowInsetsCompat ->
            WindowInsetsCompat.CONSUMED
        }
    }

    companion object {
        fun intent(context: android.content.Context, alarmId: String) =
            android.content.Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
    }
}
