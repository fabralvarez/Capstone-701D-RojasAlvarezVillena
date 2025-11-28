package com.example.vitalarmapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemMedicationResultBinding

data class MedicationSearchItem(
    val name: String,
    val description: String?
)

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

    fun updateData(newItems: List<MedicationSearchItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class MedicationViewHolder(private val binding: ItemMedicationResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicationSearchItem) {
            binding.tvMedicationName.text = item.name
            binding.tvMedicationDescription.text = item.description?.takeIf { it.isNotBlank() }
                ?: binding.root.context.getString(com.example.vitalarmapp.R.string.add_meds_empty_description)

            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
