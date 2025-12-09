package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityAlarmSumBinding
import com.example.vitalarmapp.models.AlarmRecord
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.AlarmScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import java.util.UUID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import androidx.lifecycle.lifecycleScope
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
        binding.alarmSumDateValue.text = date
        binding.alarmSumTimeValue.text = time
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
                Snackbar.make(binding.root, R.string.alarm_sum_saved, Snackbar.LENGTH_LONG).show()
                finish()
            } else {
                Snackbar.make(binding.root, R.string.alarm_sum_save_failed, Snackbar.LENGTH_LONG).show()
            }
        }
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
