package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityMainBinding
import com.example.vitalarmapp.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
        private const val DEFAULT_USER_NAME = "Usuario"
    }

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this)

        // Verificar si ya está logueado
        checkCurrentUser()
        logDarkModeConfiguration()
        initListeners()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuario ya está logueado, obtener su nombre y ir al menú
            getUserNameAndNavigate(currentUser.uid)
        } else {
            // No hay usuario logueado, mostrar pantalla de inicio normal
            Log.d(TAG, "No hay usuario logueado")
        }
    }

    private fun getUserNameAndNavigate(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userName = if (document.exists()) {
                    document.getString("name") ?: DEFAULT_USER_NAME
                } else {
                    DEFAULT_USER_NAME
                }

                Log.d(TAG, "Usuario logueado: $userName")
                navigateToMainMenu(userName)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error obteniendo nombre: ${e.message}", e)
                // Ir al menú igualmente, sin nombre
                navigateToMainMenu()
            }
    }

    private fun navigateToMainMenu(userName: String = DEFAULT_USER_NAME) {
        Intent(this, MainMenuActivity::class.java).apply {
            putExtra("userName", userName)
            startActivity(this)
        }
        finish()
    }

    // Verificar el modo oscuro
    private fun logDarkModeConfiguration() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> Log.d(TAG, "Modo claro activo")
            Configuration.UI_MODE_NIGHT_YES -> Log.d(TAG, "Modo oscuro activo")
            else -> Log.d(TAG, "Modo de interfaz desconocido")
        }
    }

    private fun initListeners() {
        binding.btnIngresar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegistrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}