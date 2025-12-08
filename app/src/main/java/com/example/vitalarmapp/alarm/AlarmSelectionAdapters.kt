package com.example.vitalarmapp.alarm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ItemMedicationRadioEntryBinding
import com.example.vitalarmapp.databinding.ItemPatientRadioEntryBinding
import com.google.android.material.color.MaterialColors

data class PatientChoice(
    val id: String,
    val name: String,
)

data class MedicationChoice(
    val id: String,
    val name: String,
    val detail: String,
)

class PatientRadioAdapter(
    private val onSelected: (PatientChoice) -> Unit,
) : RecyclerView.Adapter<PatientRadioAdapter.PatientRadioViewHolder>() {

    private val items = mutableListOf<PatientChoice>()
    private var selectedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientRadioViewHolder {
        val binding = ItemPatientRadioEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientRadioViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: PatientRadioViewHolder, position: Int) {
        holder.bind(items[position], selectedId == items[position].id) {
            selectedId = it.id
            notifyDataSetChanged()
            onSelected(it)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<PatientChoice>) {
        items.clear()
        items.addAll(newItems)
        selectedId = null
        notifyDataSetChanged()
    }

    class PatientRadioViewHolder(
        private val binding: ItemPatientRadioEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PatientChoice, isSelected: Boolean, onClick: (PatientChoice) -> Unit) {
            binding.patientName.text = item.name

            val avatarBackground = MaterialColors.getColor(
                binding.patientAvatar,
                com.google.android.material.R.attr.colorSecondaryContainer
            )
            val avatarTextColor = MaterialColors.getColor(
                binding.patientAvatar,
                com.google.android.material.R.attr.colorOnSecondaryContainer
            )
            binding.patientAvatar.setCardBackgroundColor(avatarBackground)
            binding.patientAvatarInitial.setTextColor(avatarTextColor)
            binding.patientAvatarInitial.text = item.name
                .takeIf { it.isNotBlank() }
                ?.trim()
                ?.firstOrNull()
                ?.uppercaseChar()
                ?.toString()
                ?: "?"

            binding.patientRadio.isChecked = isSelected
            binding.root.setOnClickListener { onClick(item) }
            binding.patientRadio.setOnClickListener { onClick(item) }
        }
    }
}

class MedicationRadioAdapter(
    private val onSelected: (MedicationChoice) -> Unit,
) : RecyclerView.Adapter<MedicationRadioAdapter.MedicationRadioViewHolder>() {

    private val items = mutableListOf<MedicationChoice>()
    private var selectedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationRadioViewHolder {
        val binding = ItemMedicationRadioEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicationRadioViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MedicationRadioViewHolder, position: Int) {
        holder.bind(items[position], selectedId == items[position].id) {
            selectedId = it.id
            notifyDataSetChanged()
            onSelected(it)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<MedicationChoice>) {
        items.clear()
        items.addAll(newItems)
        selectedId = null
        notifyDataSetChanged()
    }

    class MedicationRadioViewHolder(
        private val binding: ItemMedicationRadioEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicationChoice, isSelected: Boolean, onClick: (MedicationChoice) -> Unit) {
            binding.medicationName.text = item.name
            binding.medicationDetail.text = item.detail
            binding.medicationRadio.isChecked = isSelected
            binding.root.setOnClickListener { onClick(item) }
            binding.medicationRadio.setOnClickListener { onClick(item) }
        }
    }
}

fun MedicationSearchItem.toChoice(id: String): MedicationChoice {
    val dosage = listOfNotNull(dosageValue?.takeIf { it.isNotBlank() }, dosageUnit?.takeIf { it.isNotBlank() })
        .joinToString(" ")
    val formLabel = form?.name?.lowercase()?.replaceFirstChar { it.titlecase() }
    val detailParts = listOfNotNull(
        dosage.takeIf { it.isNotBlank() },
        formLabel,
        composition?.takeIf { it.isNotBlank() },
        route?.takeIf { it.isNotBlank() }
    )
    val detail = detailParts.joinToString(" • ").ifBlank { name }
    return MedicationChoice(id = id, name = name, detail = detail)
}
