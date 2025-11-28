package com.example.vitalarmapp

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.vitalarmapp.databinding.ActivityAddAlarmBinding
import com.google.android.material.chip.Chip
import java.util.Calendar

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlarmBinding
    private val alarmTimes = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlarmBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupActions()
        renderChips()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupActions() {
        binding.btnAddAlarmTime.setOnClickListener { showTimePicker() }
        binding.btnSaveAlarm.setOnClickListener { saveAlarm() }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (alarmTimes.contains(formattedTime)) {
                    Toast.makeText(this, getString(R.string.add_alarm_time_exists), Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }
                alarmTimes.add(formattedTime)
                alarmTimes.sort()
                renderChips()
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun renderChips() {
        binding.chipGroupTimes.removeAllViews()
        alarmTimes.forEach { time ->
            val chip = Chip(this).apply {
                text = time
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    alarmTimes.remove(time)
                    renderChips()
                }
            }
            binding.chipGroupTimes.addView(chip)
        }
        binding.chipGroupTimes.isVisible = alarmTimes.isNotEmpty()
    }

    private fun saveAlarm() {
        if (alarmTimes.isEmpty()) {
            Toast.makeText(this, getString(R.string.add_alarm_no_times), Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, getString(R.string.add_alarm_saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddAlarmActivity::class.java)
    }
}
