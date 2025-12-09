package com.example.vitalarmapp.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vitalarmapp.AddAlarmActivity
import com.example.vitalarmapp.AddMedsActivity
import com.example.vitalarmapp.AddPatsActivity
import com.example.vitalarmapp.NotificationsActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.SettingsActivity
import com.example.vitalarmapp.databinding.FragmentAddMainTabBinding
import com.google.android.material.transition.MaterialFadeThrough
import com.example.vitalarmapp.utils.ui.DarkModeUtils

class AddMainTabFragment : Fragment() {

    private var _binding: FragmentAddMainTabBinding? = null
    private val binding get() = _binding!!
    private var isDarkMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDarkMode = DarkModeUtils.isDarkMode(requireContext())
        val fadeThrough = MaterialFadeThrough()
        enterTransition = fadeThrough
        reenterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMainTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DarkModeUtils.applyToolbarIconColors(binding.topAppBar, isDarkMode)
        setupActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupActions() {
        binding.cardAddAlarm.setOnClickListener {
            val intent = Intent(requireContext(), AddAlarmActivity::class.java)
            startActivity(intent)
        }

        binding.cardAddPatient.setOnClickListener {
            startActivity(AddPatsActivity.intent(requireContext()))
        }

        binding.cardAddMedication.setOnClickListener {
            startActivity(AddMedsActivity.intent(requireContext()))
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> {
                    startActivity(Intent(requireContext(), NotificationsActivity::class.java))
                    true
                }
                R.id.action_settings -> {
                    startActivity(SettingsActivity.intent(requireContext()))
                    true
                }

                else -> false
            }
        }
    }
}
