package com.example.vitalarmapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.R
import com.example.vitalarmapp.models.Medication
import com.example.vitalarmapp.models.Patient

class GeneralMedicationsAdapter(
    private var medicationsList: List<Pair<Medication, Patient>>,
    private val onMedicationClick: (Pair<Medication, Patient>) -> Unit
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
        val (medication, person) = medicationsList[position]

        val medicationName = medication.name.ifEmpty { "Medicamento" }
        val dosage = medication.dosage.ifEmpty { "Sin dosis" }
        val personName = person.name.ifEmpty { "Persona" }
        val frequency = medication.frequency.ifEmpty { "Sin frecuencia" }

        val alarmTimes = medication.alarmTimes
        val timesText = if (alarmTimes.isNotEmpty()) {
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
            onMedicationClick(medicationsList[position])
        }
    }

    override fun getItemCount(): Int = medicationsList.size

    fun updateItems(newItems: List<Pair<Medication, Patient>>) {
        medicationsList = newItems
        notifyDataSetChanged()
    }
}