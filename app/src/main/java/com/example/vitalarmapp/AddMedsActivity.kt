package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.vitalarmapp.databinding.ActivityAddMedsBinding
import com.google.android.material.search.SearchView

class AddMedsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.searchBar.visibility = View.VISIBLE
        binding.searchView.visibility = View.GONE

        setupSearchBar()
        setupSearch()
    }

    private fun setupSearchBar() {
        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_search) {
                openSearchView()
                true
            } else {
                false
            }
        }
    }


    private fun setupSearch() {
        binding.searchBar.setOnClickListener { openSearchView() }

        binding.searchBar.setNavigationOnClickListener {
            if (binding.searchView.isShowing) {
                binding.searchView.hide()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.searchView.setNavigationOnClickListener { binding.searchView.hide() }

        binding.searchView.addTransitionListener { _, _, newState ->
            when (newState) {
                SearchView.TransitionState.HIDDEN -> {
                    binding.searchBar.visibility = View.VISIBLE
                    binding.searchView.visibility = View.GONE
                }

                SearchView.TransitionState.SHOWN, SearchView.TransitionState.SHOWING -> {
                    binding.searchBar.visibility = View.GONE
                    binding.searchView.visibility = View.VISIBLE
                }

                else -> Unit
            }
        }
        binding.searchView.editText.hint = getString(R.string.add_meds_search_placeholder)

        updateAddMedicationState()
    }

    private fun openSearchView() {
        if (!binding.searchView.isShowing) {
            binding.searchView.show()
        }
        binding.searchView.editText.requestFocus()
        binding.searchView.editText.post {
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(
                binding.searchView.editText,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }


    private fun updateAddMedicationState() {
        binding.addMedicationButton.isEnabled = binding.progressBar.isVisible.not() &&
                binding.searchView.editText.text.isNullOrBlank().not()
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddMedsActivity::class.java)
    }
}
