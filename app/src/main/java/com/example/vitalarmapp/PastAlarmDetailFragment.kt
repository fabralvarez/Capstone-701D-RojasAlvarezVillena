package com.example.vitalarmapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vitalarmapp.databinding.FragmentPastAlarmDetailBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.vitalarmapp.models.AlarmRecord

class PastAlarmDetailFragment : Fragment() {

    private var _binding: FragmentPastAlarmDetailBinding? = null
    private val binding get() = _binding!!

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
        viewLifecycleOwner.lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) { FirebaseManager.getAlarmById(alarmId) }
            record?.let { populateDetails(it) }
        }
    }

    private fun populateDetails(record: AlarmRecord) {
        binding.pastAlarmDetailPatient.text = record.patientName
        binding.pastAlarmDetailMedication.text = getString(
            R.string.past_alarm_detail_medication,
            record.medicationName,
            record.medicationDetail
        )
        binding.pastAlarmDetailTime.text = getString(R.string.alarm_ring_time, record.date, record.time)
        binding.pastAlarmDetailVerification.text = getString(R.string.past_alarm_detail_verified)
    }

    companion object {
        private const val ARG_ALARM_ID = "arg_alarm_id"

        fun newInstance(id: String) = PastAlarmDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_ALARM_ID, id) }
        }
    }
}
