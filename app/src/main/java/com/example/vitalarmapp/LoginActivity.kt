package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import utils.FirebaseManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {
        binding.btnEntrar.setOnClickListener { loginUser() }
    }

    private fun loginUser() {
        val email = binding.etUsuario.text.toString().trim()
        val password = binding.etContrasena.text.toString()

        if (!validateCredentials(email, password)) return

        updateLoadingState(isLoading = true)

        lifecycleScope.launch {
            val success = FirebaseManager.loginUser(email, password)
            updateLoadingState(isLoading = false)

            if (success) {
                showToast("Inicio de sesión exitoso")
                startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                finish()
            } else {
                showToast("Error en el inicio de sesión. Verifique sus credenciales")
            }
        }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            showToast("Por favor, complete todos los campos")
            return false
        }

        if (password.length < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres")
            return false
        }

        return true
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.btnEntrar.apply {
            isEnabled = !isLoading
            text = if (isLoading) "Iniciando sesión..." else "Login"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}