package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.AddAlarmActivity.Companion.EXTRA_PATIENT_NAME
import com.example.vitalarmapp.SelectMedActivity.Companion.EXTRA_MED_DETAIL
import com.example.vitalarmapp.SelectMedActivity.Companion.EXTRA_MED_NAME
import com.example.vitalarmapp.databinding.ActivityConfirmAlarmBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.platform.MaterialSharedAxis
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ConfirmAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmAlarmBinding

    private var patientId: String = ""
    private var patientName: String = ""
    private var medicationName: String = ""
    private var medicationDetail: String = ""
    private var selectedDate: LocalDate? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmAlarmBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        patientId = intent.getStringExtra(EXTRA_PATIENT_ID).orEmpty()
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        medicationName = intent.getStringExtra(EXTRA_MED_NAME).orEmpty()
        medicationDetail = intent.getStringExtra(EXTRA_MED_DETAIL).orEmpty()

        setupToolbar()
        setupContent()
        setupActions()
    }

    private fun setupToolbar() {
        binding.confirmAlarmToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupContent() {
        binding.confirmAlarmSummary.text = getString(
            R.string.confirm_alarm_summary,
            patientName,
            medicationName,
            medicationDetail
        )
    }

    private fun setupActions() {
        binding.confirmPickDate.setOnClickListener { showDatePicker() }
        binding.confirmPickTime.setOnClickListener { showTimePicker() }
        binding.confirmAlarmContinue.setOnClickListener {
            val time = selectedTime ?: return@setOnClickListener
            val date = selectedDate ?: return@setOnClickListener
            startActivity(
                AlarmSumActivity.intent(
                    context = this,
                    patientId = patientId,
                    patientName = patientName,
                    medicationName = medicationName,
                    medicationDetail = medicationDetail,
                    date = date.toString(),
                    time = time
                )
            )
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.confirm_alarm_pick_date)
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            selectedDate = date
            binding.confirmAlarmSelectedDate.text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            updateContinueState()
        }

        picker.show(supportFragmentManager, "alarm_date_picker")
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText(R.string.confirm_alarm_pick_time)
            .build()

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            binding.confirmAlarmSelectedTime.text = selectedTime
            updateContinueState()
        }

        picker.show(supportFragmentManager, "alarm_time_picker")
    }

    private fun updateContinueState() {
        binding.confirmAlarmContinue.isEnabled = selectedTime != null && selectedDate != null
    }

    companion object {
        const val EXTRA_PATIENT_ID = "com.example.vitalarmapp.PATIENT_ID"
        fun intent(
            context: Context,
            patientId: String,
            patientName: String,
            medicationName: String,
            medicationDetail: String,
        ): Intent = Intent(context, ConfirmAlarmActivity::class.java).apply {
            putExtra(EXTRA_PATIENT_ID, patientId)
            putExtra(EXTRA_PATIENT_NAME, patientName)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_MED_DETAIL, medicationDetail)
        }
    }
}
