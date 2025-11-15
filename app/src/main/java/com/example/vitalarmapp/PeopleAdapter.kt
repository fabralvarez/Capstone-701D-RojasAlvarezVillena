package com.example.vitalarmapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class PeopleAdapter(
    private var peopleList: List<Map<String, Any>>,
    private val onPersonClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>() {

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Encuentra las vistas usando findViewById
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

        // Configurar nombre
        val personName = person["name"] as? String ?: "Sin nombre"
        holder.tvPersonName.text = personName

        // Configurar fecha de nacimiento
        val birthDate = person["birthDate"] as? String
        holder.tvBirthDate.text = if (!birthDate.isNullOrEmpty()) {
            "🎂 $birthDate"
        } else {
            "🎂 No especificada"
        }

        // Click en toda la tarjeta
        holder.itemView.setOnClickListener {
            onPersonClick(person)
        }

        // Click en botón eliminar
        holder.btnDelete.setOnClickListener {
            val personId = person["id"] as? String
            if (personId != null) {
                (holder.itemView.context as? PersonListActivity)?.deletePerson(personId, position)
            }
        }
    }

    override fun getItemCount(): Int = peopleList.size

    fun updateList(newList: List<Map<String, Any>>) {
        peopleList = newList
        notifyDataSetChanged()
    }
}