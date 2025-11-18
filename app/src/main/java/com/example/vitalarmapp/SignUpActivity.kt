package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth

        initListeners()
    }

    private fun initListeners() {
        binding.signupRegisterBtn.setOnClickListener {
            registerUser()
        }

        binding.signupToolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.signupNameTf.text.toString().trim()
        val email = binding.signupEmailTf.text.toString().trim()
        val password = binding.signupPassTf.text.toString()
        val confirmPassword = binding.signupConfirmPassTf.text.toString()

        // Validaciones

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.signupRegisterBtn.isEnabled = false
        binding.signupRegisterBtn.text = "Registrando..."

        coroutineScope.launch {
            try {
                // REGISTRO DIRECTO CON FIREBASE AUTH
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                if (result.user != null) {
                    // ✅ REGISTRO EXITOSO - IR DIRECTAMENTE AL MENÚ
                    Toast.makeText(this@SignUpActivity, "✅ ¡Registro exitoso!", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this@SignUpActivity, LanMenuActivity::class.java))
                    finish()
                } else {
                    throw Exception("Usuario no creado")
                }

            } catch (e: Exception) {
                binding.signupRegisterBtn.isEnabled = true
                binding.signupRegisterBtn.text = "Registrarse"

                when {
                    e.message?.contains("email address is already") == true -> {
                        Toast.makeText(
                            this@SignUpActivity,
                            "❌ Este correo ya está registrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    e.message?.contains("network") == true -> {
                        Toast.makeText(
                            this@SignUpActivity,
                            "❌ Error de conexión",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@SignUpActivity,
                            "❌ Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

}