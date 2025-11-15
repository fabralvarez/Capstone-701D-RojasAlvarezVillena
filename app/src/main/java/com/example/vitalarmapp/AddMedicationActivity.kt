package com.example.vitalarmapp

import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityAddMedicationBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val alarmTimes = mutableListOf<String>()
    private var selectedMedicationName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val personId = intent.getStringExtra("personId") ?: ""
        val personName = intent.getStringExtra("personName") ?: ""

        binding.tvPersonName.text = "Para: $personName"

        loadBaseMedications()
        setupClickListeners(personId)
    }

    private fun loadBaseMedications() {
        coroutineScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val medications = withContext(Dispatchers.IO) {
                    FirebaseManager.getBaseMedications()
                }

                if (medications.isEmpty()) {
                    showNoMedicationsMessage()
                    return@launch
                }

                val medicationNames = medications.map {
                    it["name"] as? String ?: "Sin nombre"
                }

                // Spinner para seleccionar medicamento
                val adapter = ArrayAdapter(
                    this@AddMedicationActivity,
                    android.R.layout.simple_spinner_item,
                    medicationNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerMedications.adapter = adapter

                // Seleccionar el primero por defecto
                if (medicationNames.isNotEmpty()) {
                    selectedMedicationName = medicationNames[0]
                }

            } catch (e: Exception) {
                Log.e("AddMedication", "Error: ${e.message}")
                Toast.makeText(this@AddMedicationActivity, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun showNoMedicationsMessage() {
        binding.layoutMedicationSelection.visibility = android.view.View.GONE
        binding.layoutNoMedications.visibility = android.view.View.VISIBLE
    }

    private fun setupClickListeners(personId: String) {
        binding.btnAddTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSaveMedication.setOnClickListener {
            // Obtener el medicamento seleccionado del spinner
            selectedMedicationName = binding.spinnerMedications.selectedItem as? String ?: ""
            addMedication(personId)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnGoCreateMedications.setOnClickListener {
            startActivity(Intent(this, BaseMedicationsActivity::class.java))
            finish()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)

            if (!alarmTimes.contains(timeString)) {
                alarmTimes.add(timeString)
                updateAlarmTimesDisplay()
            }
        }, hour, minute, true)

        timePicker.show()
    }

    private fun updateAlarmTimesDisplay() {
        binding.tvAlarmTimes.text = if (alarmTimes.isNotEmpty()) {
            alarmTimes.sorted().joinToString("\n") { "⏰ $it" }
        } else {
            "No hay horarios"
        }
    }

    private fun addMedication(personId: String) {
        val dosage = binding.etDosage.text.toString().trim()
        val frequency = binding.etFrequency.text.toString().trim()

        if (selectedMedicationName.isEmpty()) {
            Toast.makeText(this, "Selecciona un medicamento", Toast.LENGTH_SHORT).show()
            return
        }

        if (dosage.isEmpty()) {
            binding.etDosage.error = "Ingresa la dosis"
            return
        }

        if (frequency.isEmpty()) {
            binding.etFrequency.error = "Ingresa la frecuencia"
            return
        }

        if (alarmTimes.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un horario", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val success = withContext(Dispatchers.IO) {
                    FirebaseManager.addMedication(personId, selectedMedicationName, dosage, frequency, alarmTimes)
                }

                if (success) {
                    Toast.makeText(this@AddMedicationActivity, "✅ Medicamento agregado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddMedicationActivity, "❌ Error al agregar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("AddMedication", "Error: ${e.message}")
                Toast.makeText(this@AddMedicationActivity, "Error al agregar", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }

    }
}