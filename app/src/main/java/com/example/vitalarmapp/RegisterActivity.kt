package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth

        initListeners()
    }

    private fun initListeners() {
        binding.btnEntrar.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.etNombre.text.toString().trim()
        val lastName = binding.etApellidos.text.toString().trim()
        val email = binding.etCorreo.text.toString().trim()
        val password = binding.etContrasena.text.toString()
        val confirmPassword = binding.etConfirmarContrasena.text.toString()

        // Validaciones
        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnEntrar.isEnabled = false
        binding.btnEntrar.text = "Registrando..."

        coroutineScope.launch {
            try {
                val fullName = "$name $lastName"

                // REGISTRO DIRECTO CON FIREBASE AUTH
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                if (result.user != null) {
                    // ✅ REGISTRO EXITOSO - IR DIRECTAMENTE AL MENÚ
                    Toast.makeText(this@RegisterActivity, "✅ ¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainMenuActivity::class.java))
                    finish()
                } else {
                    throw Exception("Usuario no creado")
                }

            } catch (e: Exception) {
                binding.btnEntrar.isEnabled = true
                binding.btnEntrar.text = "Registrarse"

                when {
                    e.message?.contains("email address is already") == true -> {
                        Toast.makeText(this@RegisterActivity, "❌ Este correo ya está registrado", Toast.LENGTH_SHORT).show()
                    }
                    e.message?.contains("network") == true -> {
                        Toast.makeText(this@RegisterActivity, "❌ Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@RegisterActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       // coroutineScope.cancel()
    }
}