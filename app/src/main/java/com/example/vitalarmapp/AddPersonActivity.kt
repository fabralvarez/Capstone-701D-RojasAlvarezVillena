package com.example.vitalarmapp

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityAddPersonBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPersonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPersonBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPersonBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Selector de fecha
        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }

        // Botón guardar
        binding.btnSavePerson.setOnClickListener {
            addPerson()
        }

        // Botón cancelar
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            binding.etBirthDate.setText(dateFormat.format(calendar.time))
        }, year, month, day)

        // Establecer fecha máxima (hoy)
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun addPerson() {
        val name = binding.etPersonName.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()

        if (name.isEmpty()) {
            binding.etPersonName.error = "Ingresa el nombre"
            return
        }

        coroutineScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val success = withContext(Dispatchers.IO) {
                    FirebaseManager.addPerson(name, if (birthDate.isNotEmpty()) birthDate else null)
                }

                if (success) {
                    Toast.makeText(this@AddPersonActivity, "Persona agregada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddPersonActivity, "Error al agregar persona", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("AddPerson", "Error agregando persona: ${e.message}")
                Toast.makeText(this@AddPersonActivity, "Error al agregar persona", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
}