package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.alarm.PatientChoice
import com.example.vitalarmapp.alarm.PatientRadioAdapter
import com.example.vitalarmapp.databinding.ActivityAddAlarmBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlarmBinding
    private val adapter = PatientRadioAdapter(::onPatientSelected)

    private var selectedPatient: PatientChoice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlarmBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupList()
        setupActions()
        loadPatients()
    }

    private fun setupToolbar() {
        binding.addAlarmToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupList() {
        binding.addAlarmPatients.layoutManager = LinearLayoutManager(this)
        binding.addAlarmPatients.adapter = adapter
    }

    private fun setupActions() {
        binding.addAlarmContinue.setOnClickListener {
            val patient = selectedPatient ?: return@setOnClickListener
            startActivity(
                SelectMedActivity.intent(
                    context = this,
                    patientId = patient.id,
                    patientName = patient.name
                )
            )
        }
    }

    private fun loadPatients() {
        lifecycleScope.launch {
            setLoading(true)
            val patients = withContext(Dispatchers.IO) {
                FirebaseManager.getPeople()
            }
            val mapped = patients.map { PatientChoice(id = it.id, name = it.name) }
            adapter.submitList(mapped)
            selectedPatient = null
            binding.addAlarmContinue.isEnabled = false
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.addAlarmLoading.isVisible = isLoading
        val hasItems = adapter.itemCount > 0
        binding.addAlarmPatients.isVisible = !isLoading && hasItems
        binding.addAlarmEmpty.isVisible = !isLoading && !hasItems
    }

    private fun onPatientSelected(patient: PatientChoice) {
        selectedPatient = patient
        binding.addAlarmContinue.isEnabled = true
    }

    companion object {
        const val EXTRA_PATIENT_ID = "extra_patient_id"
        const val EXTRA_PATIENT_NAME = "extra_patient_name"
    }
}
