package com.example.vitalarmapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemMedicationResultBinding

data class MedicationSearchItem(
    val name: String,
    val indication: String? = null,
    val pharmacology: String? = null,
    val route: String? = null,
    val composition: String? = null,
    val dosageValue: String? = null,
    val dosageUnit: String? = null,
    val form: MedicationForm? = null,
)

enum class MedicationForm(
    val allowedUnits: List<String>,
) {
    TABLET(listOf("mg", "mcg", "g")),
    CAPSULE(listOf("mg", "mcg", "g")),
    SYRUP(listOf("ml", "mg/ml")),
    DROPS(listOf("drops", "ml")),
    INJECTION(listOf("ml", "mg/ml")),
    OTHER(listOf("mg", "mcg", "g", "ml")),
}

class MedicationSearchAdapter(
    private var items: List<MedicationSearchItem>,
    private val onClick: (MedicationSearchItem) -> Unit
) : RecyclerView.Adapter<MedicationSearchAdapter.MedicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationResultBinding.inflate(
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
    fun updateData(newItems: List<MedicationSearchItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class MedicationViewHolder(private val binding: ItemMedicationResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicationSearchItem) {
            binding.tvMedicationName.text = item.name
            val summary = item.indication
                ?: item.pharmacology
                ?: item.route
                ?: item.composition
            binding.tvMedicationDescription.text = summary?.takeIf { it.isNotBlank() }
                ?: binding.root.context.getString(com.example.vitalarmapp.R.string.add_meds_empty_description)

            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
