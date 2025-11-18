package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.adapters.MedicationAdapter
import com.example.vitalarmapp.databinding.ActivityMedicationListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.vitalarmapp.utils.firebase.FirebaseManager

class MedicationListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicationListBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var adapter: MedicationAdapter

    private var personId: String = ""
    private var personName: String = ""

    companion object {
        private const val REQUEST_ADD_MEDICATION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Obtener datos de la persona
        personId = intent.getStringExtra("personId") ?: ""
        personName = intent.getStringExtra("personName") ?: ""

        if (personId.isEmpty()) {
            Toast.makeText(this, "Error: No se recibió información de la persona", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("MedicationList", "🎯 Iniciando Activity para: $personName ($personId)")

        setupUI()
        setupRecyclerView()
        initListeners()
        loadMedications()
    }

    private fun setupUI() {
        binding.tvTituloPersona.text = "Medicamentos de $personName"
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(emptyList())
        binding.rvMedicamentos.layoutManager = LinearLayoutManager(this)
        binding.rvMedicamentos.adapter = adapter
    }

    private fun initListeners() {
        binding.btnAgregarMedicamento.setOnClickListener {
            Log.d("MedicationList", "🎯 BOTÓN PRESIONADO - Navegando a AddMedication")
            val intent = Intent(this, AddMedicationActivity::class.java)
            intent.putExtra("personId", personId)
            intent.putExtra("personName", personName)
            startActivityForResult(intent, REQUEST_ADD_MEDICATION)

            // LOG EXTRA para debug
            Log.d("MedicationList", "📨 Intent enviado: $intent")
        }
    }

    private fun loadMedications() {
        Log.d("MedicationList", "🔄 Cargando medicamentos para persona: $personId")

        coroutineScope.launch {
            try {
                val medications = withContext(Dispatchers.IO) {
                    FirebaseManager.getMedicationsForPerson(personId)
                }

                Log.d("MedicationList", "✅ Medicamentos cargados: ${medications.size}")

                // ✅ SOLO mostrar el Toast si realmente NO hay medicamentos
                if (medications.isEmpty()) {
                    Toast.makeText(this@MedicationListActivity, "No hay medicamentos agregados", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("MedicationList", "📋 Mostrando ${medications.size} medicamentos en la lista")
                }

                adapter.updateList(medications)

            } catch (e: Exception) {
                Log.e("MedicationList", "❌ Error cargando medicamentos: ${e.message}")
                Toast.makeText(this@MedicationListActivity, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("MedicationList", "📨 onActivityResult - requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == REQUEST_ADD_MEDICATION && resultCode == RESULT_OK) {
            Log.d("MedicationList", "🔄 Recargando medicamentos después de agregar")
            loadMedications()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MedicationList", "🔄 onResume - Recargando medicamentos")
        loadMedications()
    }

}
