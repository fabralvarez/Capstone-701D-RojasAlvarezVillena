package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.vitalarmapp.databinding.ActivityMainMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initListeners()
        loadUserData()
        setupUserWelcome()
    }

    private fun setupUserWelcome() {
        // Obtener nombre del intent o de Firebase
        val userName = intent.getStringExtra("userName") ?: "Usuario"

        // Mostrar bienvenida personalizada
        binding.tvBienvenida.text = "Bienvenido, $userName"

        // Si no vino del intent, obtener de Firebase
        if (userName == "Usuario") {
            loadUserNameFromFirebase()
        }
    }

    private fun loadUserNameFromFirebase() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "Usuario"
                        binding.tvBienvenida.text = "Bienvenido, $userName"
                    }
                }
        }
    }

    private fun initListeners() {
        binding.btnIngresarPersona.setOnClickListener {
            startActivity(Intent(this, AddPersonActivity::class.java))
        }

        binding.btnPersonasCuidado.setOnClickListener {
            startActivity(Intent(this, PersonListActivity::class.java))
        }

        binding.btnMedicamentos.setOnClickListener {
            startActivity(Intent(this, MedicationListActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}