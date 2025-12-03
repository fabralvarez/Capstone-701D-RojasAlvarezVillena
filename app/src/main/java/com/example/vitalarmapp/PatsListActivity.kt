package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityPatsListBinding
import com.example.vitalarmapp.ui.lists.PatientListAdapter
import com.example.vitalarmapp.ui.lists.PatientListItem
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PatsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatsListBinding
    private val selectedIds = mutableSetOf<String>()
    private var isSelectionMode: Boolean = false

    private val patientAdapter by lazy {
        PatientListAdapter(::onPatientLongPressed, ::onPatientSelected)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatsListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupSelectionToolbar()
        setupRecyclerView()
        loadPatients()
    }

    private fun setupToolbar() {
        binding.patsListToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSelectionToolbar() {
        binding.patsSelectionToolbar.setNavigationOnClickListener { exitSelectionMode() }
        binding.patsSelectionToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_selection -> {
                    confirmDeletion()
                    true
                }

                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.patsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatsListActivity)
            adapter = patientAdapter
            addItemDecoration(
                MaterialDividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun loadPatients() {
        lifecycleScope.launch {
            showLoading(true)
            val patients = withContext(Dispatchers.IO) { FirebaseManager.getPeople() }
            val items = patients.map { PatientListItem(it) }
            patientAdapter.submitList(items)
            updateSelectionState(emptySet(), false)
            updateEmptyState(items.isEmpty())
            showLoading(false)
        }
    }

    private fun onPatientLongPressed(item: PatientListItem) {
        if (!isSelectionMode) {
            updateSelectionState(setOf(item.patient.id), true)
        }
    }

    private fun onPatientSelected(item: PatientListItem) {
        val updatedSelection = selectedIds.toMutableSet().apply {
            if (contains(item.patient.id)) remove(item.patient.id) else add(item.patient.id)
        }
        updateSelectionState(updatedSelection, updatedSelection.isNotEmpty())
    }

    private fun updateSelectionState(newSelection: Set<String>, selectionMode: Boolean) {
        selectedIds.clear()
        selectedIds.addAll(newSelection)
        isSelectionMode = selectionMode

        patientAdapter.updateSelection(selectedIds, isSelectionMode)
        binding.patsSelectionToolbar.isVisible = isSelectionMode
        binding.patsListCollapsingToolbar.isVisible = !isSelectionMode
        if (isSelectionMode) {
            binding.patsSelectionToolbar.title = getString(
                R.string.list_selection_count,
                selectedIds.size
            )
        }
    }

    private fun exitSelectionMode() {
        updateSelectionState(emptySet(), false)
    }

    private fun confirmDeletion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.list_delete_confirm_title)
            .setMessage(R.string.list_delete_confirm_supporting)
            .setNegativeButton(R.string.list_delete_yes) { _, _ ->
                deleteSelectedPatients()
            }
            .setPositiveButton(R.string.list_delete_no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedPatients() {
        lifecycleScope.launch {
            val itemsToDelete = patientAdapter.currentItems().filter {
                selectedIds.contains(it.patient.id)
            }

            val success = withContext(Dispatchers.IO) {
                itemsToDelete.all { FirebaseManager.deletePerson(it.patient.id) }
            }

            if (success) {
                patientAdapter.removeItems { selectedIds.contains(it.patient.id) }
                updateSelectionState(emptySet(), false)
                updateEmptyState(patientAdapter.currentItems().isEmpty())
                showSuccessDialog()
            } else {
                Snackbar.make(binding.root, R.string.list_delete_error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.list_delete_success_title)
            .setMessage(R.string.list_delete_success_supporting)
            .setPositiveButton(R.string.list_delete_success_action) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.patsRecyclerView.isVisible = !isEmpty
        binding.patsEmptyState.isVisible = isEmpty
    }

    private fun showLoading(isLoading: Boolean) {
        binding.patsLoadingIndicator.isVisible = isLoading
        binding.patsRecyclerView.isVisible = !isLoading
        binding.patsEmptyState.isVisible = !isLoading && binding.patsEmptyState.isVisible
    }

    companion object {
        fun intent(context: android.content.Context) =
            android.content.Intent(context, PatsListActivity::class.java)
    }
}
