package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.vitalarmapp.databinding.ActivityLoginBinding
import com.example.vitalarmapp.utils.PreferencesManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnEntrar.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            if (validarCredenciales(usuario, contrasena)) {
                iniciarSesion(usuario, contrasena)
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarCredenciales(usuario: String, contrasena: String): Boolean {
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        val registeredUsers = preferencesManager.getRegisteredUsers()
        return registeredUsers.any { it.correo == usuario && it.contrasena == contrasena }
    }

    private fun iniciarSesion(usuario: String, contrasena: String) {
        val registeredUsers = preferencesManager.getRegisteredUsers()
        val user = registeredUsers.firstOrNull { it.correo == usuario && it.contrasena == contrasena }

        if (user != null) {
            preferencesManager.saveUser(user)
            preferencesManager.saveSessionToken("token_${System.currentTimeMillis()}")

            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}