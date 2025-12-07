package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.AddAlarmActivity.Companion.EXTRA_PATIENT_ID
import com.example.vitalarmapp.AddAlarmActivity.Companion.EXTRA_PATIENT_NAME
import com.example.vitalarmapp.alarm.MedicationChoice
import com.example.vitalarmapp.alarm.MedicationRadioAdapter
import com.example.vitalarmapp.alarm.toChoice
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ActivitySelectMedBinding
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectMedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectMedBinding
    private val adapter = MedicationRadioAdapter(::onMedicationSelected)
    private val gson: Gson by lazy { Gson() }

    private var selectedMedication: MedicationChoice? = null
    private var patientId: String = ""
    private var patientName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
        binding = ActivitySelectMedBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        patientId = intent.getStringExtra(EXTRA_PATIENT_ID).orEmpty()
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()

        setupToolbar()
        setupList()
        setupActions()
        loadMedications()
    }

    private fun setupToolbar() {
        binding.selectMedToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupList() {
        binding.selectMedList.layoutManager = LinearLayoutManager(this)
        binding.selectMedList.adapter = adapter
        binding.selectMedList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun setupActions() {
        binding.selectMedContinue.setOnClickListener {
            val med = selectedMedication ?: return@setOnClickListener
            startActivity(
                ConfirmAlarmActivity.intent(
                    context = this,
                    patientId = patientId,
                    patientName = patientName,
                    medicationName = med.name,
                    medicationDetail = med.detail
                )
            )
        }
    }

    private fun loadMedications() {
        lifecycleScope.launch {
            setLoading(true)
            val storedItems = withContext(Dispatchers.IO) { fetchMedications() }
            adapter.submitList(storedItems)
            selectedMedication = null
            binding.selectMedContinue.isEnabled = false
            setLoading(false)
        }
    }

    private fun fetchMedications(): List<MedicationChoice> {
        val prefs = getSharedPreferences("medications_prefs", MODE_PRIVATE)
        val storedJson = prefs.getString("medications_list", "[]")
        val type = object : TypeToken<List<MedicationSearchItem>>() {}.type
        val medications = runCatching {
            gson.fromJson<List<MedicationSearchItem>>(storedJson, type)
        }.getOrDefault(emptyList())

        return medications.mapIndexed { index, item ->
            item.toChoice(id = "med_choice_${index}_${item.name}")
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.selectMedLoading.isVisible = isLoading
        val hasItems = adapter.itemCount > 0
        binding.selectMedList.isVisible = !isLoading && hasItems
        binding.selectMedEmpty.isVisible = !isLoading && !hasItems
    }

    private fun onMedicationSelected(medication: MedicationChoice) {
        selectedMedication = medication
        binding.selectMedContinue.isEnabled = true
    }

    companion object {
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_MED_DETAIL = "extra_med_detail"

        fun intent(context: Context, patientId: String, patientName: String): Intent =
            Intent(context, SelectMedActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_ID, patientId)
                putExtra(EXTRA_PATIENT_NAME, patientName)
            }
    }
}
