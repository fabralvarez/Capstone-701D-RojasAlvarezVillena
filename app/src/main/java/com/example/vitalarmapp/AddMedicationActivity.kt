package com.example.vitalarmapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityAddMedicationBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var personId: String = ""
    private var personName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Obtener datos de la persona
        personId = intent.getStringExtra("personId") ?: ""
        personName = intent.getStringExtra("personName") ?: ""

        if (personId.isEmpty()) {
            Toast.makeText(this, "Error: No se recibió información de la persona", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("AddMedication", "🎯 Iniciando para: $personName ($personId)")
        initListeners()
    }

    private fun initListeners() {
        binding.btnGuardarMedicamento.setOnClickListener {
            agregarMedicamento()
        }
    }

    private fun agregarMedicamento() {
        val nombre = binding.etNombreMedicamento.text.toString().trim()
        val dosis = binding.etDosis.text.toString().trim()
        val frecuencia = binding.etFrecuencia.text.toString().trim()
        val horariosTexto = binding.etHorarios.text.toString().trim()

        Log.d("AddMedication", "💊 Validando: '$nombre', '$dosis', '$frecuencia', '$horariosTexto'")

        // Validaciones
        if (nombre.isEmpty()) {
            showError("Por favor, ingrese el nombre del medicamento")
            return
        }

        if (dosis.isEmpty()) {
            showError("Por favor, ingrese la dosis")
            return
        }

        // Convertir horarios
        val horarios = if (horariosTexto.isNotEmpty()) {
            horariosTexto.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        Log.d("AddMedication", "🕒 Horarios: $horarios")

        // Guardar medicamento
        guardarMedicamentoEnFirebase(nombre, dosis, frecuencia, horarios)
    }

    private fun guardarMedicamentoEnFirebase(nombre: String, dosis: String, frecuencia: String, horarios: List<String>) {
        binding.btnGuardarMedicamento.isEnabled = false
        binding.btnGuardarMedicamento.text = "Guardando..."

        coroutineScope.launch {
            try {
                Log.d("AddMedication", "🚀 Iniciando guardado en Firebase...")

                val success = withContext(Dispatchers.IO) {
                    FirebaseManager.addMedication(personId, nombre, dosis, frecuencia, horarios)
                }

                Log.d("AddMedication", "📨 Resultado del guardado: $success")

                if (success) {
                    Log.d("AddMedication", "✅ Éxito - Cerrando actividad")
                    Toast.makeText(this@AddMedicationActivity, "✅ Medicamento agregado", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("AddMedication", "❌ Falló el guardado en Firebase")
                    showError("Error al guardar en la base de datos")
                }
            } catch (e: Exception) {
                Log.e("AddMedication", "❌ Error: ${e.message}", e)
                showError("Error: ${e.message}")
            }
        }
    }

    private fun showError(mensaje: String) {
        binding.btnGuardarMedicamento.isEnabled = true
        binding.btnGuardarMedicamento.text = "Guardar Medicamento"
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        //coroutineScope.cancel()
    }

}