package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityAddPersonBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPersonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPersonBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPersonBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {
        binding.btnGuardarPersona.setOnClickListener {
            agregarPersona()
        }
    }

    private fun agregarPersona() {
        val nombre = binding.etNombrePersona.text.toString().trim()
        val fechaNacimiento = binding.etFechaNacimiento.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el nombre", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar progreso
        binding.btnGuardarPersona.isEnabled = false
        binding.btnGuardarPersona.text = "Guardando..."

        // Guardar persona en Firebase
        coroutineScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    FirebaseManager.addPerson(nombre, if (fechaNacimiento.isEmpty()) null else fechaNacimiento)
                }

                binding.btnGuardarPersona.isEnabled = true
                binding.btnGuardarPersona.text = "Guardar Persona"

                if (success) {
                    Toast.makeText(this@AddPersonActivity, "✅ Persona agregada exitosamente", Toast.LENGTH_SHORT).show()
                    // Regresar al menú principal
                    finish()
                } else {
                    Toast.makeText(this@AddPersonActivity, "❌ Error al guardar persona", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnGuardarPersona.isEnabled = true
                binding.btnGuardarPersona.text = "Guardar Persona"
                Toast.makeText(this@AddPersonActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //coroutineScope.cancel()
    }
}