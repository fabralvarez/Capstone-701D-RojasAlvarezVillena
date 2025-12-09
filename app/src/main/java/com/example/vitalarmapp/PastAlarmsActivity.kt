package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.databinding.ActivityPastAlarmsBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.google.android.material.transition.platform.MaterialSharedAxis
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PastAlarmsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPastAlarmsBinding
    private val adapter by lazy { PastAlarmsAdapter(::onAlarmSelected) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        super.onCreate(savedInstanceState)
        binding = ActivityPastAlarmsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupToolbar()
        setupList()
        loadAlarms()
    }

    private fun setupToolbar() {
        binding.pastAlarmsToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupList() {
        binding.pastAlarmsList.apply {
            layoutManager = LinearLayoutManager(this@PastAlarmsActivity)
            adapter = this@PastAlarmsActivity.adapter
        }
    }

    private fun loadAlarms() {
        lifecycleScope.launch {
            showLoading(true)
            val result = withContext(Dispatchers.IO) { FirebaseManager.getAlarms() }
            adapter.submitList(result.items)
            updateEmptyState(result.items.isEmpty())
            showLoading(false)
            if (result.hasIncompleteData) {
                showDataWarning()
            }
        }
    }

    private fun onAlarmSelected(id: String) {
        startActivity(PastAlarmDetailActivity.intent(this, id))
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.pastAlarmsList.isVisible = !isEmpty
        binding.pastAlarmsEmpty.isVisible = isEmpty
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pastAlarmsLoading.isVisible = isLoading
        binding.pastAlarmsList.isVisible = !isLoading
        binding.pastAlarmsEmpty.isVisible = !isLoading && binding.pastAlarmsEmpty.isVisible
    }

    private fun showDataWarning() {
        Snackbar.make(
            binding.root,
            R.string.past_alarms_incomplete_data,
            Snackbar.LENGTH_LONG
        ).show()
    }

    companion object {
        fun intent(context: android.content.Context) =
            android.content.Intent(context, PastAlarmsActivity::class.java)
    }
}
