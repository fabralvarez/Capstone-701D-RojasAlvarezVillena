package com.example.vitalarmapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vitalarmapp.databinding.FragmentPastAlarmDetailBinding
import com.example.vitalarmapp.utils.local.AlarmRepository

class PastAlarmDetailFragment : Fragment() {

    private var _binding: FragmentPastAlarmDetailBinding? = null
    private val binding get() = _binding!!
    private val repository by lazy { AlarmRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPastAlarmDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindDetails() {
        val alarmId = arguments?.getString(ARG_ALARM_ID).orEmpty()
        val record = repository.get(alarmId) ?: return
        binding.pastAlarmDetailPatient.text = record.patientName
        binding.pastAlarmDetailMedication.text = getString(
            R.string.past_alarm_detail_medication,
            record.medicationName,
            record.medicationDetail
        )
        binding.pastAlarmDetailTime.text = record.time
        record.photoPath?.let { path ->
            binding.pastAlarmDetailPhoto.setImageURI(Uri.fromFile(java.io.File(path)))
        }
    }

    companion object {
        private const val ARG_ALARM_ID = "arg_alarm_id"

        fun newInstance(id: String) = PastAlarmDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_ALARM_ID, id) }
        }
    }
}
