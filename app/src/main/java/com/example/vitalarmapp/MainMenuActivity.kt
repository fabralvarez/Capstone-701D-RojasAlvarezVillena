package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.vitalarmapp.databinding.ActivityMainMenuBinding
import com.example.vitalarmapp.utils.PreferencesManager

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        verificarSesion()

        setupUI()
        setupClickListeners()
    }

    private fun verificarSesion() {
        if (!preferencesManager.isLoggedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupUI() {
        val usuario = preferencesManager.getCurrentUser()
        usuario?.let {
            // Personalizar la UI con datos del usuario si es necesario
        }

        // Datos de ejemplo para el próximo medicamento
        binding.tvMedicamentoNombre.text = "Paracetamol 500mg"
        binding.tvHoraAdministracion.text = "10:30 AM"
        binding.tvPersonaAdministrar.text = "Para: María González"
    }

    private fun setupClickListeners() {
        binding.btnIngresarPersona.setOnClickListener {
            val intent = Intent(this, AddPersonActivity::class.java)
            startActivity(intent)
        }

        binding.btnPersonasCuidado.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.btnMedicamentos.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        preferencesManager.logout()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        // No hacer nada o mostrar mensaje de que use el botón de cerrar sesión
        Toast.makeText(this, "Use el botón 'Cerrar Sesión' para salir", Toast.LENGTH_SHORT).show()
    }
}