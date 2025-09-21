package com.example.vitalarmapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityRegisterBinding
import com.example.vitalarmapp.models.User
import com.example.vitalarmapp.utils.PreferencesManager
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferencesManager: PreferencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        preferencesManager = PreferencesManager(this)
        darkModeChecker()
        initListeners()
    }

    private fun darkModeChecker() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_YES -> {}
        }
    }

    private fun initListeners() {
        binding.btnEntrar.setOnClickListener {
            if (validarCampos()) {
                registrarUsuario()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString()
        val confirmarContrasena = binding.etConfirmarContrasena.text.toString()

        if (nombre.isEmpty()) {
            binding.etNombre.error = "Ingrese su nombre"
            return false
        }

        if (apellidos.isEmpty()) {
            binding.etApellidos.error = "Ingrese sus apellidos"
            return false
        }

        if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.etCorreo.error = "Ingrese un correo válido"
            return false
        }

        if (preferencesManager.userExists(correo)) {
            binding.etCorreo.error = "Este correo ya está registrado"
            return false
        }

        if (contrasena.length < 6) {
            binding.etContrasena.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        if (contrasena != confirmarContrasena) {
            binding.etConfirmarContrasena.error = "Las contraseñas no coinciden"
            return false
        }

        return true
    }

    private fun registrarUsuario() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString()

        val nuevoUsuario = User(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            apellidos = apellidos,
            correo = correo,
            contrasena = contrasena
        )

        // Guardar el nuevo usuario
        val usuariosExistentes = preferencesManager.getRegisteredUsers().toMutableList()
        usuariosExistentes.add(nuevoUsuario)
        preferencesManager.saveRegisteredUsers(usuariosExistentes)

        // Iniciar sesión automáticamente
        preferencesManager.saveUser(nuevoUsuario)
        preferencesManager.saveSessionToken("token_${System.currentTimeMillis()}")

        Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}