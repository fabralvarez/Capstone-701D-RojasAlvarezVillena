package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.example.vitalarmapp.databinding.ActivityNotificationsBinding
import com.example.vitalarmapp.notifications.NotificationHistoryRepository
import com.example.vitalarmapp.notifications.NotificationsAdapter
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val repository = NotificationHistoryRepository()
    private val notificationsAdapter = NotificationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupAppBar()
        setupRecycler()
        loadNotifications()
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        binding.notificationsRecycler.apply {
            adapter = notificationsAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
        }
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) {
                repository.loadEntries(this@NotificationsActivity)
            }

            val fadeThrough = MaterialFadeThrough().apply {
                duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
            }

            TransitionManager.beginDelayedTransition(binding.notificationsContainer, fadeThrough)
            notificationsAdapter.submitList(entries)
            binding.notificationsEmpty.isVisible = entries.isEmpty()
            binding.notificationsRecycler.isVisible = entries.isNotEmpty()
        }
    }
}
