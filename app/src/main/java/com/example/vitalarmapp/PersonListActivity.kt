package com.example.vitalarmapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.adapters.PeopleAdapter
import com.example.vitalarmapp.databinding.ActivityPersonListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.vitalarmapp.models.Patient
import com.example.vitalarmapp.utils.firebase.FirebaseManager

class PersonListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonListBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var peopleAdapter: PeopleAdapter
    private var peopleList = mutableListOf<Patient>()

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
            val intent = AddMedsActivity.intent(this)
            intent.putExtra("personId", person.id)
            intent.putExtra("personName", person.name)
            startActivity(intent)
        }

        binding.rvPeople.apply {
            layoutManager = LinearLayoutManager(this@PersonListActivity)
            adapter = peopleAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddPerson.setOnClickListener {
            startActivity(AddPatsActivity.intent(this))
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
                peopleAdapter.updateData(peopleList)

                if (people.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = android.view.View.GONE
                }

            } catch (e: Exception) {
                Log.e("PersonList", "Error cargando personas: ${e.message}")
                Snackbar.make(binding.root, "Error al cargar personas", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.btnAddPerson)
                    .show()
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
                    Snackbar.make(binding.root, "Persona eliminada", Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.btnAddPerson)
                        .show()

                    if (peopleList.isEmpty()) {
                        binding.tvEmptyState.visibility = android.view.View.VISIBLE
                    }
                } else {
                    Snackbar.make(binding.root, "Error al eliminar persona", Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.btnAddPerson)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("PersonList", "Error eliminando persona: ${e.message}")
                Snackbar.make(binding.root, "Error al eliminar persona", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.btnAddPerson)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPeople()
    }
}