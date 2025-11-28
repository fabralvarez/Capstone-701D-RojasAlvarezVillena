package com.example.vitalarmapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.databinding.ActivityLanMenuBinding
import com.example.vitalarmapp.models.Medication
import com.example.vitalarmapp.models.Patient
import com.example.vitalarmapp.navigation.BottomNavigationHelper
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.SessionManager
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLanMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanMenuBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupBottomNavigation()
        initListeners()
        loadUserName()
        loadNextMedication()
    }

    private fun setupBottomNavigation() {
        BottomNavigationHelper.setup(
            binding.lanMenuBottomNavigation,
            this
        ) {
            loadNextMedication()
            loadUserName()
        }
    }

    private fun loadUserName() {
        lifecycleScope.launch {
            try {
                val userName = withContext(Dispatchers.IO) {
                    FirebaseManager.getCurrentUserName()
                }
                binding.tvBienvenida.text = "Bienvenido, $userName!"
            } catch (_: Exception) {
                binding.tvBienvenida.text = "Bienvenido, Usuario"
            }
        }
    }

    private fun initListeners() {
        binding.btnIngresarPersona.setOnClickListener {
            startActivity(AddPatsActivity.intent(this))
        }

        binding.btnPersonasCuidado.setOnClickListener {
            startActivity(Intent(this, PersonListActivity::class.java))
        }

        binding.btnMedicamentos.setOnClickListener {
            startActivity(AddMedsActivity.intent(this))
        }

        binding.btnCerrarSesion.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadNextMedication() {
        lifecycleScope.launch {
            try {
                val people = withContext(Dispatchers.IO) {
                    FirebaseManager.getPeople()
                }

                if (people.isEmpty()) {
                    showNoMedicationsMessage("No hay personas agregadas")
                    return@launch
                }

                // Buscar TODOS los medicamentos
                val allMedications = mutableListOf<MedicationWithTime>()

                for (person in people) {
                    val medications = withContext(Dispatchers.IO) {
                        FirebaseManager.getMedicationsForPerson(person.id)
                    }

                    for (medication in medications) {
                        val alarmTimes = medication.alarmTimes
                        if (alarmTimes.isNotEmpty()) {
                            for (time in alarmTimes) {
                                allMedications.add(MedicationWithTime(medication, person, time))
                            }
                        }
                    }
                }

                if (allMedications.isEmpty()) {
                    showNoMedicationsMessage("No hay medicamentos programados")
                    return@launch
                }

                // Encontrar el más cercano
                val nextMedication = findNextMedication(allMedications)

                if (nextMedication != null) {
                    showNextMedication(
                        nextMedication.medication,
                        nextMedication.patient,
                        nextMedication.time
                    )
                } else {
                    showNoMedicationsMessage("No hay medicamentos programados")
                }

            } catch (_: Exception) {
                showNoMedicationsMessage("Error al cargar medicamentos")
            }
        }
    }

    private fun findNextMedication(medications: List<MedicationWithTime>): MedicationWithTime? {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        Log.d("MainMenu", "🕐 Hora actual: $currentHour:$currentMinute")

        var nextMedication: MedicationWithTime? = null
        var minDifference = Int.MAX_VALUE

        for (med in medications) {
            val timeMinutes = convertTimeToMinutes(med.time)
            var difference = timeMinutes - currentTotalMinutes

            // Si ya pasó hoy, sumar 24 horas
            if (difference < 0) {
                difference += 24 * 60
            }

            Log.d(
                "MainMenu",
                "💊 ${med.medication.name} - ${med.time} - diferencia: $difference min"
            )

            if (difference < minDifference) {
                minDifference = difference
                nextMedication = med
            }
        }

        return nextMedication
    }

    private fun convertTimeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            hours * 60 + minutes
        } catch (_: Exception) {
            0
        }
    }

    private fun showNextMedication(medication: Medication, patient: Patient, nextTime: String) {
        val medicationName = medication.name.ifEmpty { "Medicamento" }
        val dosage = medication.dosage.ifEmpty { "Sin dosis" }
        val personName = patient.name.ifEmpty { "Persona" }

        // Calcular tiempo de forma SIMPLE
        val timeText = calculateSimpleTime(nextTime)

        // Actualizar UI
        binding.tvMedicamentoNombre.text = medicationName
        binding.tvHoraAdministracion.text = "⏰ $nextTime"
        binding.tvPersonaAdministrar.text = "👤 $personName\n💊 $dosage\n$timeText"

        // Hacer clickable
        binding.tvMedicamentoNombre.setOnClickListener {
            startActivity(AddMainTabActivity.intent(this))
        }
        binding.tvHoraAdministracion.setOnClickListener {
            startActivity(AddMainTabActivity.intent(this))
        }
        binding.tvPersonaAdministrar.setOnClickListener {
            startActivity(AddMainTabActivity.intent(this))
        }

        Log.d("MainMenu", "✅ Próximo: $medicationName a las $nextTime para $personName")
    }

    private fun calculateSimpleTime(nextTime: String): String {
        return try {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val currentTotalMinutes = currentHour * 60 + currentMinute

            val nextTimeMinutes = convertTimeToMinutes(nextTime)
            var minutesLeft = nextTimeMinutes - currentTotalMinutes

            if (minutesLeft < 0) {
                minutesLeft += 24 * 60
            }

            // Formato SIMPLE sin "en"
            when {
                minutesLeft <= 0 -> "¡Ahora!"
                minutesLeft < 60 -> "$minutesLeft minutos"
                else -> {
                    val hours = minutesLeft / 60
                    val mins = minutesLeft % 60
                    if (mins == 0) "$hours horas" else "$hours h $mins min"
                }
            }
        } catch (_: Exception) {
            "Próximo"
        }
    }

    private fun showNoMedicationsMessage(message: String) {
        binding.tvMedicamentoNombre.text = "💊 No hay medicamentos"
        binding.tvHoraAdministracion.text = message
        binding.tvPersonaAdministrar.text = "Agrega personas y medicamentos"

        binding.tvMedicamentoNombre.setOnClickListener {
            startActivity(Intent(this, PersonListActivity::class.java))
        }
    }

    private fun logoutUser() {
        FirebaseManager.logout()
        SessionManager.setKeepSession(this, false)
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.lanMenuBottomNavigation.selectedItemId = R.id.nav_home
        loadNextMedication()
        loadUserName()
    }

    private data class MedicationWithTime(
        val medication: Medication,
        val patient: Patient,
        val time: String
    )
}