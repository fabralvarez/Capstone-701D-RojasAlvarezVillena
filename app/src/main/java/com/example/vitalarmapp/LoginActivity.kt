package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityLoginBinding
import utils.FirebaseManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {
        binding.btnEntrar.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = binding.etUsuario.text.toString().trim()
        val password = binding.etContrasena.text.toString()

        // Validaciones
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar progreso
        binding.btnEntrar.isEnabled = false
        binding.btnEntrar.text = "Iniciando sesión..."

        // Login con Firebase
        scope.launch {
            val success = FirebaseManager.loginUser(email, password)

            // Volver al hilo principal para mostrar resultado
            runOnUiThread {
                binding.btnEntrar.isEnabled = true
                binding.btnEntrar.text = "Login"

                if (success) {
                    Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Error en el inicio de sesión. Verifique sus credenciales", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}