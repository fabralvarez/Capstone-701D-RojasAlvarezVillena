package com.example.vitalarmapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemMedicationBinding

class MedicationAdapter(
    private var medicationList: List<Map<String, Any>>
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(private val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(medication: Map<String, Any>) {
            binding.tvNombreMedicamento.text = medication["name"] as? String ?: "Sin nombre"
            binding.tvDosis.text = "💊 Dosis: ${medication["dosage"] as? String ?: "No especificada"}"
            binding.tvFrecuencia.text = "🕒 Frecuencia: ${medication["frequency"] as? String ?: "No especificada"}"

            val alarmTimes = medication["alarmTimes"] as? List<*>
            binding.tvHorarios.text = if (!alarmTimes.isNullOrEmpty()) {
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

    fun updateList(newList: List<Map<String, Any>>) {
        medicationList = newList
        notifyDataSetChanged()
    }
}