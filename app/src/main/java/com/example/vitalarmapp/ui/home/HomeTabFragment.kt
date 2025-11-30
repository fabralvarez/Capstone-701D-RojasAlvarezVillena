package com.example.vitalarmapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.AddMedsActivity
import com.example.vitalarmapp.AddPatsActivity
import com.example.vitalarmapp.LanMenuActivity
import com.example.vitalarmapp.MainActivity
import com.example.vitalarmapp.PersonListActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.databinding.FragmentHomeTabBinding
import com.example.vitalarmapp.models.Medication
import com.example.vitalarmapp.models.Patient
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeTabFragment : Fragment() {

    private var _binding: FragmentHomeTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fadeThrough = MaterialFadeThrough()
        enterTransition = fadeThrough
        reenterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        refreshContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshContent() {
        if (view == null) return
        loadUserName()
        loadNextMedication()
    }

    private fun initListeners() {
        binding.btnIngresarPersona.setOnClickListener {
            startActivity(AddPatsActivity.intent(requireContext()))
        }

        binding.btnPersonasCuidado.setOnClickListener {
            startActivity(Intent(requireContext(), PersonListActivity::class.java))
        }

        binding.btnMedicamentos.setOnClickListener {
            startActivity(AddMedsActivity.intent(requireContext()))
        }

        binding.btnCerrarSesion.setOnClickListener {
            logoutUser()
        }

        val openAddTab: (View) -> Unit = {
            (activity as? LanMenuActivity)?.selectTab(R.id.nav_add)
        }
        binding.tvMedicamentoNombre.setOnClickListener(openAddTab)
        binding.tvHoraAdministracion.setOnClickListener(openAddTab)
        binding.tvPersonaAdministrar.setOnClickListener(openAddTab)
    }

    private fun loadUserName() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userName = withContext(Dispatchers.IO) {
                    FirebaseManager.getCurrentUserName()
                }
                binding.tvBienvenida.text = getString(R.string.home_greeting, userName)
            } catch (_: Exception) {
                binding.tvBienvenida.text = getString(R.string.home_greeting_fallback)
            }
        }
    }

    private fun loadNextMedication() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val people = withContext(Dispatchers.IO) {
                    FirebaseManager.getPeople()
                }

                if (people.isEmpty()) {
                    showNoMedicationsMessage(getString(R.string.home_no_people))
                    return@launch
                }

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
                    showNoMedicationsMessage(getString(R.string.home_no_medications))
                    return@launch
                }

                val nextMedication = findNextMedication(allMedications)

                if (nextMedication != null) {
                    showNextMedication(
                        nextMedication.medication,
                        nextMedication.patient,
                        nextMedication.time
                    )
                } else {
                    showNoMedicationsMessage(getString(R.string.home_no_medications))
                }

            } catch (_: Exception) {
                showNoMedicationsMessage(getString(R.string.home_error_loading))
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
        val medicationName = medication.name.ifEmpty { getString(R.string.home_medication_placeholder) }
        val dosage = medication.dosage.ifEmpty { getString(R.string.home_dosage_placeholder) }
        val personName = patient.name.ifEmpty { getString(R.string.home_person_placeholder) }

        val timeText = calculateSimpleTime(nextTime)

        binding.tvMedicamentoNombre.text = medicationName
        binding.tvHoraAdministracion.text = getString(R.string.home_next_time, nextTime)
        binding.tvPersonaAdministrar.text = getString(
            R.string.home_person_and_dosage,
            personName,
            dosage,
            timeText
        )

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

            when {
                minutesLeft <= 0 -> getString(R.string.home_now)
                minutesLeft < 60 -> getString(R.string.home_minutes_format, minutesLeft)
                else -> {
                    val hours = minutesLeft / 60
                    val mins = minutesLeft % 60
                    if (mins == 0) getString(R.string.home_hours_format, hours)
                    else getString(R.string.home_hours_minutes_format, hours, mins)
                }
            }
        } catch (_: Exception) {
            getString(R.string.home_next_label)
        }
    }

    private fun showNoMedicationsMessage(message: String) {
        binding.tvMedicamentoNombre.text = getString(R.string.home_no_medications_title)
        binding.tvHoraAdministracion.text = message
        binding.tvPersonaAdministrar.text = getString(R.string.home_add_people_hint)

        binding.tvMedicamentoNombre.setOnClickListener {
            startActivity(Intent(requireContext(), PersonListActivity::class.java))
        }
    }

    private fun logoutUser() {
        FirebaseManager.logout()
        SessionManager.setKeepSession(requireContext(), false)
        Snackbar.make(binding.root, getString(R.string.profile_logout_message), Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.btnCerrarSesion)
            .show()
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    private data class MedicationWithTime(
        val medication: Medication,
        val patient: Patient,
        val time: String
    )
}
