package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityBaseMedicationsBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BaseMedicationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseMedicationsBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var medicationsAdapter: BaseMedicationsAdapter
    private var medicationsList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseMedicationsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        loadMedications()
        setupClickListeners()
    }
    private fun showMedicationUsers(medicationName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = FirebaseManager.getMedicationUsers(medicationName)

                launch(Dispatchers.Main) {
                    if (users.isEmpty()) {
                        Toast.makeText(
                            this@BaseMedicationsActivity,
                            "💊 $medicationName\nNadie usa este medicamento todavía",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showUsersDialog(medicationName, users)
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@BaseMedicationsActivity, "Error al cargar información", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUsersDialog(medicationName: String, users: List<Map<String, Any>>) {
        val message = StringBuilder()
        message.append("💊 $medicationName\n\n")
        message.append("Personas que usan este medicamento:\n\n")

        users.forEachIndexed { index, user ->
            val personName = user["personName"] as? String ?: "Sin nombre"
            val dosage = user["dosage"] as? String ?: "Sin dosis"
            val frequency = user["frequency"] as? String ?: "Sin frecuencia"
            val alarmTimes = user["alarmTimes"] as? List<String> ?: emptyList()

            message.append("${index + 1}. 👤 $personName\n")
            message.append("   💊 Dosis: $dosage\n")
            message.append("   📅 Frecuencia: $frequency\n")

            if (alarmTimes.isNotEmpty()) {
                message.append("   ⏰ Horarios: ${alarmTimes.joinToString(", ")}\n")
            }

            message.append("\n")
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Uso del Medicamento")
            .setMessage(message.toString())
            .setPositiveButton("Cerrar", null)
            .create()

        dialog.show()
    }
    private fun setupRecyclerView() {
        medicationsAdapter = BaseMedicationsAdapter(
            medicationsList,
            onMedicationClick = { medication ->
                val medicationName = medication["name"] as? String ?: "Medicamento"
                showMedicationUsers(medicationName)  // <- Mostrar quién lo usa
            },
            onDeleteClick = { medicationId, position ->
                deleteMedication(medicationId, position)
            }
        )

        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(this@BaseMedicationsActivity)
            adapter = medicationsAdapter
        }
    }

    private fun deleteMedication(medicationId: String, position: Int) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Medicamento")
            .setMessage("¿Estás seguro de que quieres eliminar este medicamento?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                performDelete(medicationId, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        alertDialog.show()
    }

    private fun performDelete(medicationId: String, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = FirebaseManager.deleteBaseMedication(medicationId)

                launch(Dispatchers.Main) {
                    if (success) {
                        medicationsList.removeAt(position)
                        medicationsAdapter.notifyItemRemoved(position)
                        Toast.makeText(this@BaseMedicationsActivity, "✅ Medicamento eliminado", Toast.LENGTH_SHORT).show()

                        if (medicationsList.isEmpty()) {
                            binding.tvEmptyState.visibility = android.view.View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@BaseMedicationsActivity, "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@BaseMedicationsActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Botón para CREAR medicamento
        binding.btnAddMedication.setOnClickListener {
            showAddMedicationDialog()
        }

        // Botón volver
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showAddMedicationDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Crear Medicamento")
            .setMessage("Nombre del medicamento:")
            .setView(R.layout.dialog_add_base_medication)
            .setPositiveButton("Crear") { dialogInterface, _ ->
                val dialog = dialogInterface as androidx.appcompat.app.AlertDialog
                val editText = dialog.findViewById<android.widget.EditText>(R.id.etMedicationName)
                val name = editText?.text?.toString()?.trim() ?: ""

                if (name.isNotEmpty()) {
                    addBaseMedication(name)
                } else {
                    Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun addBaseMedication(name: String) {
        coroutineScope.launch {
            try {
                Log.d("BaseMedications", "🟡 Intentando crear medicamento: $name")

                val success = withContext(Dispatchers.IO) {
                    FirebaseManager.addBaseMedication(name)
                }

                if (success) {
                    Log.d("BaseMedications", "✅ Medicamento '$name' creado exitosamente")
                    Toast.makeText(this@BaseMedicationsActivity, "✅ '$name' creado", Toast.LENGTH_SHORT).show()
                    loadMedications()
                } else {
                    Log.e("BaseMedications", "❌ Error al crear medicamento (success=false)")
                    Toast.makeText(this@BaseMedicationsActivity, "❌ Error al crear", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BaseMedications", "❌ Excepción al crear medicamento: ${e.message}", e)
                Toast.makeText(this@BaseMedicationsActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMedications() {
        coroutineScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val medications = withContext(Dispatchers.IO) {
                    FirebaseManager.getBaseMedications()
                }

                medicationsList.clear()
                medicationsList.addAll(medications)
                medicationsAdapter.notifyDataSetChanged()

                if (medications.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = android.view.View.GONE
                }

            } catch (e: Exception) {
                Log.e("BaseMedications", "Error cargando: ${e.message}")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMedications()
    }
}