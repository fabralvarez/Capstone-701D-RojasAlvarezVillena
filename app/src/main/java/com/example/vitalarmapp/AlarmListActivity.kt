package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityAlarmListBinding
import com.example.vitalarmapp.ui.lists.AlarmListAdapter
import com.example.vitalarmapp.ui.lists.AlarmListItem
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.AlarmScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmListBinding
    private val selectedIds = mutableSetOf<String>()
    private var isSelectionMode: Boolean = false

    private val alarmAdapter by lazy {
        AlarmListAdapter(::onAlarmLongPressed, ::onAlarmSelected)
    }

    private val alarmScheduler by lazy { AlarmScheduler(this) }

    private val displayLocale = Locale("es", "US")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", displayLocale)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupSelectionToolbar()
        setupRecyclerView()
        loadAlarms()
    }

    private fun setupToolbar() {
        binding.alarmListToolbar.setNavigationOnClickListener { finish() }
        binding.alarmListToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_past_alarms -> {
                    startActivity(PastAlarmsActivity.intent(this))
                    true
                }

                else -> false
            }
        }
    }

    private fun setupSelectionToolbar() {
        binding.alarmSelectionToolbar.setNavigationOnClickListener { exitSelectionMode() }
        binding.alarmSelectionToolbar.setOnMenuItemClickListener { item ->
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
        binding.alarmRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AlarmListActivity)
            adapter = alarmAdapter
            addItemDecoration(
                MaterialDividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun loadAlarms() {
        lifecycleScope.launch {
            showLoading(true)
            val items = withContext(Dispatchers.IO) { buildAlarmItems() }
            alarmAdapter.submitList(items)
            updateSelectionState(emptySet(), false)
            updateEmptyState(items.isEmpty())
            showLoading(false)
        }
    }

    private suspend fun buildAlarmItems(): List<AlarmListItem> {
        val alarmRecords = FirebaseManager.getUpcomingAlarms(limit = Int.MAX_VALUE)

        return alarmRecords.map { record ->
            val schedule = parseSchedule(record.date, record.time, record.scheduledAt)
            val scheduleText = formatSchedule(schedule, record.date, record.time)

            AlarmListItem(
                id = record.id,
                patientId = record.patientId,
                medicationId = record.medicationName,
                patientName = record.patientName,
                medicationName = record.medicationName,
                scheduleText = scheduleText,
                scheduledAt = schedule,
                originalTime = record.time,
            )
        }.sortedBy { it.scheduledAt ?: LocalDateTime.MAX }
    }

    private fun parseSchedule(date: String, time: String, fallbackMillis: Long): LocalDateTime? {
        return runCatching {
            val parsedDate = LocalDate.parse(date)
            val parsedTime = LocalTime.parse(time)
            LocalDateTime.of(parsedDate, parsedTime)
        }.getOrNull() ?: runCatching {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(fallbackMillis),
                java.time.ZoneId.systemDefault(),
            )
        }.getOrNull()
    }

    private fun formatSchedule(dateTime: LocalDateTime?, fallbackDate: String, fallbackTime: String): String {
        return dateTime?.let { dateTimeFormatter.format(it) }
            ?: "$fallbackDate $fallbackTime"
    }

    private fun onAlarmLongPressed(item: AlarmListItem) {
        if (!isSelectionMode) {
            updateSelectionState(setOf(item.id), true)
        }
    }

    private fun onAlarmSelected(item: AlarmListItem) {
        val updatedSelection = selectedIds.toMutableSet().apply {
            if (contains(item.id)) remove(item.id) else add(item.id)
        }
        updateSelectionState(updatedSelection, updatedSelection.isNotEmpty())
    }

    private fun updateSelectionState(newSelection: Set<String>, selectionMode: Boolean) {
        selectedIds.clear()
        selectedIds.addAll(newSelection)
        isSelectionMode = selectionMode

        alarmAdapter.updateSelection(selectedIds, isSelectionMode)
        binding.alarmSelectionToolbar.isVisible = isSelectionMode
        binding.alarmListCollapsingToolbar.isVisible = !isSelectionMode
        if (isSelectionMode) {
            binding.alarmSelectionToolbar.title = getString(
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
                deleteSelectedAlarms()
            }
            .setPositiveButton(R.string.list_delete_no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedAlarms() {
        lifecycleScope.launch {
            val selectedAlarms = selectedIds.toList()

            val success = withContext(Dispatchers.IO) {
                FirebaseManager.deleteAlarms(selectedAlarms)
            }

            if (success) {
                alarmScheduler.cancelAlarms(selectedAlarms)
                loadAlarms()
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
        binding.alarmRecyclerView.isVisible = !isEmpty
        binding.alarmEmptyState.isVisible = isEmpty
    }

    private fun showLoading(isLoading: Boolean) {
        binding.alarmLoadingIndicator.isVisible = isLoading
        binding.alarmRecyclerView.isVisible = !isLoading
        binding.alarmEmptyState.isVisible = !isLoading && binding.alarmEmptyState.isVisible
    }

    companion object {
        fun intent(context: android.content.Context) =
            android.content.Intent(context, AlarmListActivity::class.java)
    }
}
