package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityPersonListBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonListBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var adapter: PersonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        initListeners()
        loadPeople()
    }

    private fun setupRecyclerView() {
        adapter = PersonAdapter(emptyList()) { person ->
            // Al hacer clic en una persona
            val personId = person["id"] as? String
            val personName = person["name"] as? String

            if (personId != null && personName != null) {
                val intent = Intent(this, MedicationListActivity::class.java)
                intent.putExtra("personId", personId)
                intent.putExtra("personName", personName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: Datos de persona inválidos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvPersonas.layoutManager = LinearLayoutManager(this)
        binding.rvPersonas.adapter = adapter
    }

    private fun initListeners() {
        binding.btnAgregarPersona.setOnClickListener {
            startActivity(Intent(this, AddPersonActivity::class.java))
        }
    }

    private fun loadPeople() {
        coroutineScope.launch {
            try {
                Log.d("PersonList", "🔄 Cargando personas...")

                val people = withContext(Dispatchers.IO) {
                    FirebaseManager.getPeople()
                }

                Log.d("PersonList", "✅ Personas cargadas: ${people.size}")

                if (people.isEmpty()) {
                    Toast.makeText(this@PersonListActivity, "No hay personas agregadas", Toast.LENGTH_SHORT).show()
                }

                adapter.updateList(people)

            } catch (e: Exception) {
                Log.e("PersonList", "❌ Error cargando personas: ${e.message}")
                Toast.makeText(this@PersonListActivity, "Error al cargar personas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPeople()
    }

    override fun onDestroy() {
        super.onDestroy()
        //coroutineScope.cancel()
    }
}