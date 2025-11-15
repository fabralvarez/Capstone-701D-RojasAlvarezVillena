package com.example.vitalarmapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GeneralMedicationsAdapter(
    private var medicationsList: List<Map<String, Any>>,
    private val onMedicationClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<GeneralMedicationsAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMedicationName: TextView = itemView.findViewById(R.id.tvMedicationName)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val tvPerson: TextView = itemView.findViewById(R.id.tvPerson)
        val tvTimes: TextView = itemView.findViewById(R.id.tvTimes)
        val tvFrequency: TextView = itemView.findViewById(R.id.tvFrequency)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_general_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medicationsList[position]

        val medicationName = medication["name"] as? String ?: "Medicamento"
        val dosage = medication["dosage"] as? String ?: "Sin dosis"
        val personName = medication["personName"] as? String ?: "Persona"
        val frequency = medication["frequency"] as? String ?: "Sin frecuencia"

        val alarmTimes = medication["alarmTimes"] as? List<String>
        val timesText = if (!alarmTimes.isNullOrEmpty()) {
            alarmTimes.sorted().joinToString(" • ") { "⏰ $it" }
        } else {
            "Sin horarios"
        }

        holder.tvMedicationName.text = medicationName
        holder.tvDosage.text = dosage
        holder.tvPerson.text = "👤 $personName"
        holder.tvTimes.text = timesText
        holder.tvFrequency.text = frequency

        holder.itemView.setOnClickListener {
            onMedicationClick(medication)
        }
    }

    override fun getItemCount(): Int = medicationsList.size

    fun updateList(newList: List<Map<String, Any>>) {
        medicationsList = newList
        notifyDataSetChanged()
    }
}