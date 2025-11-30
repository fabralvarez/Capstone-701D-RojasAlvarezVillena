package com.example.vitalarmapp

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityAddPatsBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPatsBinding
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPatsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.btnSavePerson.setOnClickListener { savePatient() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            binding.etBirthDate.setText(dateFormat.format(calendar.time))
        }, year, month, day).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun savePatient() {
        val name = binding.etPersonName.text?.toString()?.trim().orEmpty()
        val birthDate = binding.etBirthDate.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) {
            binding.etPersonName.error = getString(R.string.add_patient_name_error)
            return
        }

        lifecycleScope.launch {
            toggleLoading(true)
            try {
                val isSuccessful = withContext(Dispatchers.IO) {
                    FirebaseManager.addPerson(name, birthDate.ifEmpty { null })
                }

                val message = if (isSuccessful) {
                    R.string.add_patient_success
                } else {
                    R.string.add_patient_failure
                }

                Snackbar.make(binding.root, getString(message), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.btnSavePerson)
                    .show()

                if (isSuccessful) finish()
            } catch (error: Exception) {
                Snackbar.make(binding.root, getString(R.string.add_patient_failure), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.btnSavePerson)
                    .show()
            } finally {
                toggleLoading(false)
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnSavePerson.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddPatsActivity::class.java)
    }
}
