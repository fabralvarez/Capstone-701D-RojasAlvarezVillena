package com.example.vitalarmapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BaseMedicationsAdapter(
    private var medicationsList: List<Map<String, Any>>,
    private val onMedicationClick: (Map<String, Any>) -> Unit,
    private val onDeleteClick: (String, Int) -> Unit  // <- Nueva función para eliminar
) : RecyclerView.Adapter<BaseMedicationsAdapter.MedicationViewHolder>() {

    class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMedicationName: TextView = itemView.findViewById(R.id.tvMedicationName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)  // <- Botón eliminar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_base_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medicationsList[position]
        val medicationId = medication["id"] as? String ?: ""

        holder.tvMedicationName.text = medication["name"] as? String ?: "Sin nombre"

        val description = medication["description"] as? String
        holder.tvDescription.text = description ?: "Medicamento base"

        // Click en el item
        holder.itemView.setOnClickListener {
            onMedicationClick(medication)
        }

        // Click en eliminar
        holder.btnDelete.setOnClickListener {
            onDeleteClick(medicationId, position)
        }
    }

    override fun getItemCount(): Int = medicationsList.size
}