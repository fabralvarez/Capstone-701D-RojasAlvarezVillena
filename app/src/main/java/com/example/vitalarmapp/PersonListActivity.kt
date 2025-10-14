package com.example.vitalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityPersonListBinding
import com.example.vitalarmapp.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonListBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var peopleAdapter: PeopleAdapter
    private var peopleList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupRecyclerView()
        loadPeople()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        peopleAdapter = PeopleAdapter(peopleList) { person ->
            // Click en persona - ir a medicamentos
            val intent = Intent(this, MedicationListActivity::class.java)
            intent.putExtra("personId", person["id"] as? String)
            intent.putExtra("personName", person["name"] as? String)
            startActivity(intent)
        }

        binding.rvPeople.apply {
            layoutManager = LinearLayoutManager(this@PersonListActivity)
            adapter = peopleAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddPerson.setOnClickListener {
            startActivity(Intent(this, AddPersonActivity::class.java))
        }

    }

    private fun loadPeople() {
        coroutineScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE

                val people = withContext(Dispatchers.IO) {
                    FirebaseManager.getPeople()
                }

                peopleList.clear()
                peopleList.addAll(people)
                peopleAdapter.notifyDataSetChanged()

                if (people.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = android.view.View.GONE
                }

            } catch (e: Exception) {
                Log.e("PersonList", "Error cargando personas: ${e.message}")
                Toast.makeText(this@PersonListActivity, "Error al cargar personas", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    // Función para eliminar persona
    fun deletePerson(personId: String, position: Int) {
        coroutineScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    FirebaseManager.deletePerson(personId)
                }

                if (success) {
                    peopleList.removeAt(position)
                    peopleAdapter.notifyItemRemoved(position)
                    Toast.makeText(this@PersonListActivity, "Persona eliminada", Toast.LENGTH_SHORT).show()

                    if (peopleList.isEmpty()) {
                        binding.tvEmptyState.visibility = android.view.View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@PersonListActivity, "Error al eliminar persona", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PersonList", "Error eliminando persona: ${e.message}")
                Toast.makeText(this@PersonListActivity, "Error al eliminar persona", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPeople()
    }
}