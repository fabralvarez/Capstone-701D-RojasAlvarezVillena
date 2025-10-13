package com.example.vitalarmapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemPersonBinding

class PersonAdapter(
    private var personList: List<Map<String, Any>>,
    private val onPersonClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    class PersonViewHolder(private val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(person: Map<String, Any>, onPersonClick: (Map<String, Any>) -> Unit) {
            binding.tvNombrePersona.text = person["name"] as? String ?: "Sin nombre"

            val birthDate = person["birthDate"] as? String
            binding.tvFechaNacimiento.text = if (!birthDate.isNullOrEmpty()) {
                "🎂 $birthDate"
            } else {
                "Sin fecha de nacimiento"
            }

            itemView.setOnClickListener {
                onPersonClick(person)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(personList[position], onPersonClick)
    }

    override fun getItemCount(): Int = personList.size

    fun updateList(newList: List<Map<String, Any>>) {
        personList = newList
        notifyDataSetChanged()
    }
}