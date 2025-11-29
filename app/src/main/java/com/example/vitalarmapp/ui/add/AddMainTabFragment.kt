package com.example.vitalarmapp.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vitalarmapp.AddAlarmActivity
import com.example.vitalarmapp.AddMedsActivity
import com.example.vitalarmapp.AddPatsActivity
import com.example.vitalarmapp.databinding.FragmentAddMainTabBinding

class AddMainTabFragment : Fragment() {

    private var _binding: FragmentAddMainTabBinding? = null
    private val binding get() = _binding!!

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
        setupActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupActions() {
        binding.cardAddAlarm.setOnClickListener {
            startActivity(AddAlarmActivity.intent(requireContext()))
        }

        binding.cardAddPatient.setOnClickListener {
            startActivity(AddPatsActivity.intent(requireContext()))
        }

        binding.cardAddMedication.setOnClickListener {
            startActivity(AddMedsActivity.intent(requireContext()))
        }
    }
}
