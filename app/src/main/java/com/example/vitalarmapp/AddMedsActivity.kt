package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.adapters.MedicationSearchAdapter
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ActivityAddMedsBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMedsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedsBinding
    private val allMedications = mutableListOf<MedicationSearchItem>()
    private val adapter by lazy { MedicationSearchAdapter(emptyList(), ::onMedicationSelected) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupRecycler()
        setupSearch()
        loadBaseMedications()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterResults(query)
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterResults(newText)
                    return true
                }
            })
        }
    }

    private fun loadBaseMedications() {
        lifecycleScope.launch {
            toggleLoading(true)
            try {
                val medications = withContext(Dispatchers.IO) {
                    FirebaseManager.getBaseMedications().map { baseMedication ->
                        MedicationSearchItem(
                            name = baseMedication["name"] as? String
                                ?: getString(R.string.add_meds_unknown),
                            description = baseMedication["description"] as? String
                        )
                    }
                }.sortedBy { it.name.lowercase() }

                allMedications.clear()
                allMedications.addAll(medications)
                filterResults(binding.searchView.query?.toString())
            } catch (error: Exception) {
                Toast.makeText(
                    this@AddMedsActivity,
                    getString(R.string.add_meds_load_error),
                    Toast.LENGTH_SHORT
                ).show()
                adapter.updateData(emptyList())
            } finally {
                toggleLoading(false)
            }
        }
    }

    private fun filterResults(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allMedications
        } else {
            val lowerQuery = query.lowercase()
            allMedications.filter { medication ->
                medication.name.lowercase().contains(lowerQuery) ||
                    medication.description?.lowercase()?.contains(lowerQuery) == true
            }
        }
        adapter.updateData(filtered)
    }

    private fun onMedicationSelected(item: MedicationSearchItem) {
        val message = getString(R.string.add_meds_selected_format, item.name)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.searchView.isEnabled = !isLoading
        binding.rvMedications.isVisible = !isLoading
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddMedsActivity::class.java)
    }
}
