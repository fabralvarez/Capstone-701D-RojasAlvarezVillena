package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ActivityMedsListBinding
import com.example.vitalarmapp.ui.lists.MedicationListAdapter
import com.example.vitalarmapp.ui.lists.MedicationListItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MedsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedsListBinding
    private val gson: Gson by lazy { Gson() }
    private val selectedIds = mutableSetOf<String>()
    private var isSelectionMode: Boolean = false

    private val medicationAdapter by lazy {
        MedicationListAdapter(::onMedicationLongPressed, ::onMedicationSelected)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedsListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupSelectionToolbar()
        setupRecyclerView()
        loadMedications()
    }

    private fun setupToolbar() {
        binding.medsListToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSelectionToolbar() {
        binding.medsSelectionToolbar.setNavigationOnClickListener { exitSelectionMode() }
        binding.medsSelectionToolbar.setOnMenuItemClickListener { item ->
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
        binding.medsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MedsListActivity)
            adapter = medicationAdapter
            addItemDecoration(
                MaterialDividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun loadMedications() {
        showLoading(true)
        val prefs = getSharedPreferences("medications_prefs", MODE_PRIVATE)
        val storedJson = prefs.getString("medications_list", "[]")
        val type = object : TypeToken<List<MedicationSearchItem>>() {}.type
        val medications = runCatching {
            gson.fromJson<List<MedicationSearchItem>>(storedJson, type)
        }.getOrDefault(emptyList())

        val items = medications.mapIndexed { index, item ->
            MedicationListItem(
                id = "med_${index}_${item.name}",
                medication = item,
            )
        }

        medicationAdapter.submitList(items)
        updateSelectionState(emptySet(), false)
        updateEmptyState(items.isEmpty())
        showLoading(false)
    }

    private fun onMedicationLongPressed(item: MedicationListItem) {
        if (!isSelectionMode) {
            updateSelectionState(setOf(item.id), true)
        }
    }

    private fun onMedicationSelected(item: MedicationListItem) {
        val updatedSelection = selectedIds.toMutableSet().apply {
            if (contains(item.id)) remove(item.id) else add(item.id)
        }
        updateSelectionState(updatedSelection, updatedSelection.isNotEmpty())
    }

    private fun updateSelectionState(newSelection: Set<String>, selectionMode: Boolean) {
        selectedIds.clear()
        selectedIds.addAll(newSelection)
        isSelectionMode = selectionMode

        medicationAdapter.updateSelection(selectedIds, isSelectionMode)
        binding.medsSelectionToolbar.isVisible = isSelectionMode
        binding.medsListCollapsingToolbar.isVisible = !isSelectionMode
        if (isSelectionMode) {
            binding.medsSelectionToolbar.title = getString(
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
                deleteSelectedMedications()
            }
            .setPositiveButton(R.string.list_delete_no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedMedications() {
        val currentItems = medicationAdapter.currentItems()
        val remainingItems = currentItems.filterNot { selectedIds.contains(it.id) }

        val prefs = getSharedPreferences("medications_prefs", MODE_PRIVATE)
        val committed = prefs.edit()
            .putString("medications_list", gson.toJson(remainingItems.map { it.medication }))
            .commit()

        if (committed) {
            medicationAdapter.removeItems { selectedIds.contains(it.id) }
            updateSelectionState(emptySet(), false)
            updateEmptyState(remainingItems.isEmpty())
            showSuccessDialog()
        } else {
            Snackbar.make(binding.root, R.string.list_delete_error, Snackbar.LENGTH_LONG).show()
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
        binding.medsRecyclerView.isVisible = !isEmpty
        binding.medsEmptyState.isVisible = isEmpty
    }

    private fun showLoading(isLoading: Boolean) {
        binding.medsLoadingIndicator.isVisible = isLoading
        binding.medsRecyclerView.isVisible = !isLoading
        binding.medsEmptyState.isVisible = !isLoading && binding.medsEmptyState.isVisible
    }

    companion object {
        fun intent(context: android.content.Context) =
            android.content.Intent(context, MedsListActivity::class.java)
    }
}
