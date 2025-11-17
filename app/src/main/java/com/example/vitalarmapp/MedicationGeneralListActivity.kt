package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityMedicationGeneralListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Medication
import models.Person
import utils.FirebaseManager

class MedicationGeneralListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicationGeneralListBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var medicationsAdapter: GeneralMedicationsAdapter
    private var allMedications = mutableListOf<Pair<Medication, Person>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationGeneralListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        loadAllMedications()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        medicationsAdapter = GeneralMedicationsAdapter(allMedications) { (medication, person) ->
            // Click en medicamento - podrías mostrar detalles o editar
            val personName = person.name.ifEmpty { "Persona" }
            val medicationName = medication.name.ifEmpty { "Medicamento" }
            Toast.makeText(this, "$medicationName - $personName", Toast.LENGTH_SHORT).show()
        }

        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(this@MedicationGeneralListActivity)
            adapter = medicationsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddMedication.setOnClickListener {
            // Navegar a la lista de personas para seleccionar a quién agregar medicamento
            startActivity(Intent(this, PersonListActivity::class.java))
        }
    }

    private fun loadAllMedications() {
        coroutineScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                // Obtener todas las personas
                val people = withContext(Dispatchers.IO) {
                    FirebaseManager.getPeople()
                }

                allMedications.clear()

                // Para cada persona, obtener sus medicamentos
                for (person in people) {
                    val medications = withContext(Dispatchers.IO) {
                        FirebaseManager.getMedicationsForPerson(person.id)
                    }

                    medications.forEach { medication ->
                        allMedications.add(medication to person)
                    }
                }

                // Ordenar por hora más temprana
                allMedications.sortBy { (medication, _) ->
                    medication.alarmTimes.firstOrNull() ?: "23:59"
                }

                medicationsAdapter.updateItems(allMedications)

                if (allMedications.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = android.view.View.GONE
                }

                Log.d("MedicationGeneral", "✅ Medicamentos cargados: ${allMedications.size}")

            } catch (e: Exception) {
                Log.e("MedicationGeneral", "❌ Error cargando medicamentos: ${e.message}")
                Toast.makeText(this@MedicationGeneralListActivity, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAllMedications()
    }
}