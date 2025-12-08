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
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.platform.MaterialSharedAxis
import java.util.Locale

class ConfirmAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmAlarmBinding

    private var patientName: String = ""
    private var medicationName: String = ""
    private var medicationDetail: String = ""
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmAlarmBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

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
        binding.confirmPickTime.setOnClickListener { showTimePicker() }
        binding.confirmAlarmContinue.setOnClickListener {
            val time = selectedTime ?: return@setOnClickListener
            startActivity(
                AlarmSumActivity.intent(
                    context = this,
                    patientName = patientName,
                    medicationName = medicationName,
                    medicationDetail = medicationDetail,
                    time = time
                )
            )
        }
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
            binding.confirmAlarmContinue.isEnabled = true
        }

        picker.show(supportFragmentManager, "alarm_time_picker")
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
