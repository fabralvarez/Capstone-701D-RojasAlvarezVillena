package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityAlarmSumBinding
import com.example.vitalarmapp.utils.local.AlarmRecord
import com.example.vitalarmapp.utils.local.AlarmRepository
import com.example.vitalarmapp.utils.local.AlarmScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import java.util.UUID

class AlarmSumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmSumBinding

    private var patientName: String = ""
    private var medicationName: String = ""
    private var medicationDetail: String = ""
    private var time: String = ""
    private val repository by lazy { AlarmRepository(this) }
    private val scheduler by lazy { AlarmScheduler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSumBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        medicationName = intent.getStringExtra(EXTRA_MED_NAME).orEmpty()
        medicationDetail = intent.getStringExtra(EXTRA_MED_DETAIL).orEmpty()
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
        binding.alarmSumTimeValue.text = time
    }

    private fun setupActions() {
        binding.alarmSumSave.setOnClickListener {
            persistAlarm()
        }
    }

    private fun persistAlarm() {
        val record = AlarmRecord(
            id = UUID.randomUUID().toString(),
            patientName = patientName,
            medicationName = medicationName,
            medicationDetail = medicationDetail,
            time = time,
            scheduledAt = System.currentTimeMillis()
        )
        repository.save(record)
        scheduler.schedule(record)
        Snackbar.make(binding.root, R.string.alarm_sum_saved, Snackbar.LENGTH_LONG).show()
        finish()
    }

    companion object {
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_MED_DETAIL = "extra_med_detail"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_PATIENT_NAME = "extra_patient_name"

        fun intent(
            context: Context,
            patientName: String,
            medicationName: String,
            medicationDetail: String,
            time: String,
        ): Intent = Intent(context, AlarmSumActivity::class.java).apply {
            putExtra(EXTRA_PATIENT_NAME, patientName)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_MED_DETAIL, medicationDetail)
            putExtra(EXTRA_TIME, time)
        }
    }
}
