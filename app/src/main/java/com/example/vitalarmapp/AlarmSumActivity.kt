package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityAlarmSumBinding
import com.example.vitalarmapp.models.AlarmRecord
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.AlarmScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmSumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmSumBinding

    private var patientId: String = ""
    private var patientName: String = ""
    private var medicationName: String = ""
    private var medicationDetail: String = ""
    private var date: String = ""
    private var time: String = ""
    private val scheduler by lazy { AlarmScheduler(this) }
    private val displayLocale by lazy { resources.configuration.locales[0] ?: Locale.getDefault() }
    private val dateFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(displayLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSumBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        patientId = intent.getStringExtra(EXTRA_PATIENT_ID).orEmpty()
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        medicationName = intent.getStringExtra(EXTRA_MED_NAME).orEmpty()
        medicationDetail = intent.getStringExtra(EXTRA_MED_DETAIL).orEmpty()
        date = intent.getStringExtra(EXTRA_DATE).orEmpty()
        time = intent.getStringExtra(EXTRA_TIME).orEmpty()

        setupToolbar()
        renderSummary()
        setupActions()
    }

    private fun setupToolbar() {
        binding.alarmSumToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun renderSummary() {
        binding.alarmSumPatientValue.text = patientName
        binding.alarmSumMedValue.text = getString(R.string.alarm_sum_med_value, medicationName, medicationDetail)
        binding.alarmSumDateValue.text = formatDateForDisplay(date)
        binding.alarmSumTimeValue.text = formatTimeForDisplay(time)
    }

    private fun setupActions() {
        binding.alarmSumSave.setOnClickListener {
            persistAlarm()
        }
    }

    private fun persistAlarm() {
        val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return
        val parsedTime = runCatching { LocalTime.parse(time) }.getOrNull() ?: return
        val scheduledAt = LocalDateTime.of(parsedDate, parsedTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (!isFutureSchedule(scheduledAt)) {
            Snackbar.make(binding.root, R.string.alarm_sum_invalid_time, Snackbar.LENGTH_LONG)
                .show()
            return
        }

        val record = AlarmRecord(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            patientName = patientName,
            medicationName = medicationName,
            medicationDetail = medicationDetail,
            date = date,
            time = time,
            scheduledAt = scheduledAt,
        )

        lifecycleScope.launch {
            binding.alarmSumSave.isEnabled = false
            val saved = withContext(Dispatchers.IO) { FirebaseManager.addAlarm(record) }
            binding.alarmSumSave.isEnabled = true
            if (saved) {
                scheduler.schedule(record)
                showSuccessDialog()
            } else {
                Snackbar.make(binding.root, R.string.alarm_sum_save_failed, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun isFutureSchedule(targetMillis: Long): Boolean {
        val now = System.currentTimeMillis()
        val minimumAllowed = now + TimeUnit.MINUTES.toMillis(1)
        return targetMillis >= minimumAllowed
    }

    private fun showSuccessDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.alarm_sum_saved_title)
            .setMessage(R.string.alarm_sum_saved_message)
            .setNegativeButton(R.string.alarm_sum_saved_add_another) { _, _ ->
                restartAlarmFlow()
            }
            .setPositiveButton(R.string.alarm_sum_saved_return) { _, _ ->
                navigateToAddMainTab()
            }
            .setCancelable(false)
            .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                navigateToAddMainTab()
                true
            } else {
                false
            }
        }
        dialog.show()
    }

    private fun restartAlarmFlow() {
        startActivity(Intent(this, AddAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        finish()
    }

    private fun formatDateForDisplay(raw: String): String {
        val parsed = runCatching { LocalDate.parse(raw) }.getOrNull() ?: return raw
        return parsed.format(dateFormatter)
    }

    private fun formatTimeForDisplay(raw: String): String {
        val parsed = runCatching { LocalTime.parse(raw) }.getOrNull() ?: return raw
        val formatter = if (DateFormat.is24HourFormat(this)) {
            DateTimeFormatter.ofPattern("HH:mm", displayLocale)
        } else {
            DateTimeFormatter.ofPattern("hh:mm a", displayLocale)
        }
        return parsed.format(formatter)
    }

    private fun navigateToAddMainTab() {
        startActivity(
            AddMainTabActivity.intent(this).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finishAffinity()
    }

    companion object {
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_MED_DETAIL = "extra_med_detail"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_PATIENT_NAME = "extra_patient_name"
        const val EXTRA_PATIENT_ID = "extra_patient_id"
        const val EXTRA_DATE = "extra_date"

        fun intent(
            context: Context,
            patientId: String,
            patientName: String,
            medicationName: String,
            medicationDetail: String,
            date: String,
            time: String,
        ): Intent = Intent(context, AlarmSumActivity::class.java).apply {
            putExtra(EXTRA_PATIENT_ID, patientId)
            putExtra(EXTRA_PATIENT_NAME, patientName)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_MED_DETAIL, medicationDetail)
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_TIME, time)
        }
    }
}
