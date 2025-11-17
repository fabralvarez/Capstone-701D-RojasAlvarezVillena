package com.example.vitalarmapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemMedicationBinding
import models.Medication

class MedicationAdapter(
    private var medicationList: List<Medication>
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(private val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(medication: Medication) {
            binding.tvNombreMedicamento.text = medication.name.ifEmpty { "Sin nombre" }
            binding.tvDosis.text = "💊 Dosis: ${medication.dosage.ifEmpty { "No especificada" }}"
            binding.tvFrecuencia.text = "🕒 Frecuencia: ${medication.frequency.ifEmpty { "No especificada" }}"

            val alarmTimes = medication.alarmTimes
            binding.tvHorarios.text = if (alarmTimes.isNotEmpty()) {
                "⏰ Horarios: ${alarmTimes.joinToString(", ")}"
            } else {
                "⏰ Sin horarios configurados"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(medicationList[position])
    }

    override fun getItemCount(): Int = medicationList.size

    fun updateList(newList: List<Medication>) {
        medicationList = newList
        notifyDataSetChanged()
    }
}