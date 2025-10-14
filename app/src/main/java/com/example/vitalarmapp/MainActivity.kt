package com.example.vitalarmapp

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityMainBinding
import com.example.vitalarmapp.utils.NotificationHelper  // <- IMPORT FALTANTE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this)

        // Verificar si ya está logueado
        checkCurrentUser()
        darkModeChecker()
        initListeners()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuario ya está logueado, obtener su nombre y ir al menú
            getUserNameAndNavigate(currentUser.uid)
        } else {
            // No hay usuario logueado, mostrar pantalla de inicio normal
            Log.d("MainActivity", "No hay usuario logueado")
        }
    }

    private fun getUserNameAndNavigate(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userName = if (document.exists()) {
                    document.getString("name") ?: "Usuario"
                } else {
                    "Usuario"
                }

                Log.d("MainActivity", "Usuario logueado: $userName")

                // Ir al menú principal con el nombre del usuario
                val intent = Intent(this, MainMenuActivity::class.java)
                intent.putExtra("userName", userName)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error obteniendo nombre: ${e.message}")
                // Ir al menú igualmente, sin nombre
                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

    // Verificar el modo oscuro
    private fun darkModeChecker() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_YES -> {}
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