package com.example.vitalarmapp.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.R
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ItemRegisteredMedicationBinding
import com.example.vitalarmapp.databinding.ItemRegisteredPatientBinding
import com.example.vitalarmapp.databinding.ItemUpcomingAlarmBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class UpcomingAlarmUiModel(
    val patientName: String,
    val medicationName: String,
    val scheduledAt: LocalDateTime?
)

internal data class PatientSummaryUiModel(
    val name: String,
    val ageLabel: String
)

internal class UpcomingAlarmAdapter :
    RecyclerView.Adapter<UpcomingAlarmAdapter.UpcomingAlarmViewHolder>() {

    private val items = mutableListOf<UpcomingAlarmUiModel>()
    private val displayLocale = Locale("es", "US")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", displayLocale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", displayLocale)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingAlarmViewHolder {
        val binding = ItemUpcomingAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UpcomingAlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpcomingAlarmViewHolder, position: Int) {
        holder.bind(items[position], dateFormatter, timeFormatter)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<UpcomingAlarmUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class UpcomingAlarmViewHolder(private val binding: ItemUpcomingAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: UpcomingAlarmUiModel,
            dateFormatter: DateTimeFormatter,
            timeFormatter: DateTimeFormatter
        ) {
            binding.alarmPatientName.text = item.patientName
            binding.alarmMedicationName.text = item.medicationName
            binding.alarmSchedule.text = formatSchedule(
                binding.root.context,
                item.scheduledAt,
                dateFormatter,
                timeFormatter
            )
        }

        private fun formatSchedule(
            context: Context,
            scheduledAt: LocalDateTime?,
            dateFormatter: DateTimeFormatter,
            timeFormatter: DateTimeFormatter
        ): String {
            if (scheduledAt == null) {
                return context.getString(R.string.home_alarm_schedule_unknown)
            }

            val date = scheduledAt.toLocalDate()
            val today = LocalDate.now()
            val dateLabel = when (date) {
                today -> context.getString(R.string.home_alarm_today)
                today.plusDays(1) -> context.getString(R.string.home_alarm_tomorrow)
                else -> dateFormatter.format(date)
            }
            val timeLabel = timeFormatter.format(scheduledAt.toLocalTime())
            return context.getString(R.string.home_alarm_schedule_format, dateLabel, timeLabel)
        }
    }
}

internal class RegisteredPatientsAdapter :
    RecyclerView.Adapter<RegisteredPatientsAdapter.PatientViewHolder>() {

    private val items = mutableListOf<PatientSummaryUiModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemRegisteredPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<PatientSummaryUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class PatientViewHolder(private val binding: ItemRegisteredPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PatientSummaryUiModel) {
            binding.patientName.text = item.name
            binding.patientAge.text = item.ageLabel
        }
    }
}

internal class RegisteredMedicationsAdapter :
    RecyclerView.Adapter<RegisteredMedicationsAdapter.MedicationViewHolder>() {

    private val items = mutableListOf<MedicationSearchItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemRegisteredMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<MedicationSearchItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class MedicationViewHolder(private val binding: ItemRegisteredMedicationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicationSearchItem) {
            binding.medicationName.text = item.name
        }
    }
}
