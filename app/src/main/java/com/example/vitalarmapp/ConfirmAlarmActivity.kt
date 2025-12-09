package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.addCallback
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class ConfirmAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmAlarmBinding
    private val displayLocale by lazy { resources.configuration.locales[0] ?: Locale.getDefault() }
    private val dateFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(displayLocale)
    }

    private var patientId: String = ""
    private var patientName: String = ""
    private var medicationName: String = ""
    private var medicationDetail: String = ""
    private var soundTitle: String = ""
    private var soundUri: String = ""
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmAlarmBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) { navigateToMenu() }

        patientId = intent.getStringExtra(EXTRA_PATIENT_ID).orEmpty()
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        medicationName = intent.getStringExtra(EXTRA_MED_NAME).orEmpty()
        medicationDetail = intent.getStringExtra(EXTRA_MED_DETAIL).orEmpty()
        soundTitle = intent.getStringExtra(EXTRA_SOUND_TITLE).orEmpty()
        soundUri = intent.getStringExtra(EXTRA_SOUND_URI).orEmpty()

        setupToolbar()
        setupContent()
        setupActions()
    }

    private fun setupToolbar() {
        binding.confirmAlarmToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupContent() {
        binding.confirmAlarmSummary.text = getString(
            R.string.confirm_alarm_summary,
            patientName,
            medicationName,
            medicationDetail
        )
        binding.confirmAlarmSoundValue.text = soundTitle.ifBlank { getString(R.string.select_sound_default_label) }
    }

    private fun setupActions() {
        binding.confirmPickDateTime.setOnClickListener { showDatePicker() }
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
                    soundUri = soundUri,
                    soundTitle = soundTitle,
                    date = date.toString(),
                    time = time.toString()
                )
            )
            finish()
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.confirm_alarm_pick_date)
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            selectedDate = date
            binding.confirmAlarmSelectedDate.text = formatDate(date)
            updateContinueState()
            showTimePicker()
        }

        picker.show(supportFragmentManager, "alarm_date_picker")
    }

    private fun showTimePicker() {
        val builder = MaterialTimePicker.Builder()
            .setTimeFormat(if (DateFormat.is24HourFormat(this)) {
                TimeFormat.CLOCK_24H
            } else {
                TimeFormat.CLOCK_12H
            })
            .setTitleText(R.string.confirm_alarm_pick_time)

        selectedTime?.let { time ->
            builder.setHour(time.hour)
            builder.setMinute(time.minute)
        }

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            selectedTime = LocalTime.of(hour, minute)
            binding.confirmAlarmSelectedTime.text = formatTime(selectedTime)
            updateContinueState()
        }

        picker.show(supportFragmentManager, "alarm_time_picker")
    }

    private fun updateContinueState() {
        binding.confirmAlarmContinue.isEnabled = selectedTime != null && selectedDate != null
    }

    private fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    private fun formatTime(time: LocalTime?): String {
        val localTime = time ?: return ""
        val locale = displayLocale
        val formatter = if (DateFormat.is24HourFormat(this)) {
            DateTimeFormatter.ofPattern("HH:mm", locale)
        } else {
            DateTimeFormatter.ofPattern("hh:mm a", locale)
        }
        return localTime.format(formatter)
    }

    private fun navigateToMenu() {
        val intent = LanMenuActivity.intentForTab(this, R.id.nav_home).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_PATIENT_ID = "com.example.vitalarmapp.PATIENT_ID"
        const val EXTRA_SOUND_URI = "com.example.vitalarmapp.SOUND_URI"
        const val EXTRA_SOUND_TITLE = "com.example.vitalarmapp.SOUND_TITLE"
        fun intent(
            context: Context,
            patientId: String,
            patientName: String,
            medicationName: String,
            medicationDetail: String,
            soundUri: String,
            soundTitle: String,
        ): Intent = Intent(context, ConfirmAlarmActivity::class.java).apply {
            putExtra(EXTRA_PATIENT_ID, patientId)
            putExtra(EXTRA_PATIENT_NAME, patientName)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_MED_DETAIL, medicationDetail)
            putExtra(EXTRA_SOUND_URI, soundUri)
            putExtra(EXTRA_SOUND_TITLE, soundTitle)
        }
    }
}
