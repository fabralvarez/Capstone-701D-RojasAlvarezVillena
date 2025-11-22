package com.example.vitalarmapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.PersonListActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.models.Patient

class PeopleAdapter(
    private var peopleList: List<Patient>,
    private val onPersonClick: (Patient) -> Unit
) : RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>() {

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPersonName: TextView = itemView.findViewById(R.id.tvPersonName)
        val tvBirthDate: TextView = itemView.findViewById(R.id.tvBirthDate)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = peopleList[position]
        val personName = person.name.ifEmpty { "Sin nombre" }
        holder.tvPersonName.text = personName
        val birthDate = person.birthDate
        holder.tvBirthDate.text = if (!birthDate.isNullOrEmpty()) {
            "🎂 $birthDate"
        } else {
            "🎂 No especificada"
        }
        holder.itemView.setOnClickListener {
            onPersonClick(person)
        }
        holder.btnDelete.setOnClickListener {
            val personId = person.id
            if (personId.isNotEmpty()) {
                (holder.itemView.context as? PersonListActivity)?.deletePerson(personId, position)
            }
        }
    }

    override fun getItemCount(): Int = peopleList.size

    fun updateData(newList: List<Patient>) {
        peopleList = newList
        notifyDataSetChanged()
    }
}