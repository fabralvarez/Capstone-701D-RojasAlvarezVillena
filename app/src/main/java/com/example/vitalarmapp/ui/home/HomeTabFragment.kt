package com.example.vitalarmapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.AlarmListActivity
import com.example.vitalarmapp.MedsListActivity
import com.example.vitalarmapp.PatsListActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.databinding.FragmentHomeTabBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.google.android.material.transition.MaterialFadeThrough
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeTabFragment : Fragment() {

    private var _binding: FragmentHomeTabBinding? = null
    private val binding get() = _binding!!

    private val upcomingAlarmsAdapter = UpcomingAlarmAdapter()
    private val patientsAdapter = RegisteredPatientsAdapter()
    private val medicationsAdapter = RegisteredMedicationsAdapter()

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
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserNameLoading(true)
        setupRecyclerViews()
        setupSectionNavigation()
        refreshContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshContent() {
        if (view == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            loadRegisteredPatients()
            loadRegisteredMedications()
            loadUpcomingAlarms()
        }
    }

    fun setUserNameLoading(isLoading: Boolean) {
        if (view == null) return
        binding.homeLoadingIndicator.isVisible = isLoading
        binding.homeContent.isVisible = !isLoading
    }

    fun onUserNameLoaded() {
        setUserNameLoading(false)
        refreshContent()
    }

    private fun setupRecyclerViews() {
        binding.upcomingAlarmsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingAlarmsAdapter
        }

        binding.registeredPatientsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = patientsAdapter
        }

        binding.registeredMedicationsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = medicationsAdapter
        }
    }

    private fun setupSectionNavigation() {
        binding.upcomingAlarmsHeader.setOnClickListener {
            startActivity(AlarmListActivity.intent(requireContext()))
        }

        binding.registeredPatientsHeader.setOnClickListener {
            startActivity(PatsListActivity.intent(requireContext()))
        }

        binding.registeredMedicationsHeader.setOnClickListener {
            startActivity(MedsListActivity.intent(requireContext()))
        }
    }

    private suspend fun loadRegisteredPatients() {
        val patients = withContext(Dispatchers.IO) { FirebaseManager.getPeople() }
        val summaries = patients.map { patient ->
            val ageText = calculateAge(patient.birthDate)
                ?.let { getString(R.string.home_patient_age_format, it) }
                ?: getString(R.string.home_patient_age_unknown)
            PatientSummaryUiModel(
                name = patient.name,
                ageLabel = ageText,
            )
        }
        patientsAdapter.submitList(summaries)
        binding.registeredPatientsEmpty.isVisible = summaries.isEmpty()
    }

    private fun calculateAge(birthDate: String?): Int? {
        if (birthDate.isNullOrBlank()) return null
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        return runCatching {
            val birth = LocalDate.parse(birthDate, formatter)
            val today = LocalDate.now()
            Period.between(birth, today).years
        }.getOrNull()?.takeIf { it >= 0 }
    }

    private suspend fun loadRegisteredMedications() {
        val meds = FirebaseManager.getRegisteredMedications()
        val medicationItems = meds.map { it.medication }

        medicationsAdapter.submitList(medicationItems)
        binding.registeredMedicationsEmpty.isVisible = medicationItems.isEmpty()
    }

    private fun loadUpcomingAlarms() {
        val alarms: List<UpcomingAlarmUiModel> = emptyList()
        upcomingAlarmsAdapter.submitList(alarms)
        binding.upcomingAlarmsEmpty.isVisible = true
    }
}
