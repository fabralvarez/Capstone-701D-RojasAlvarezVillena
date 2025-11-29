package com.example.vitalarmapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.MainActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.databinding.FragmentProfileTabBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileTabFragment : Fragment() {

    private var _binding: FragmentProfileTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActions()
        loadProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupActions() {
        binding.btnProfileLogout.setOnClickListener { logoutUser() }
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = withContext(Dispatchers.IO) { FirebaseManager.getCurrentUserName() }
            val email = FirebaseAuth.getInstance().currentUser?.email
                ?: getString(R.string.profile_unknown_email)

            binding.tvProfileName.text = getString(R.string.profile_greeting_format, name)
            binding.tvProfileEmail.text = email
        }
    }

    private fun logoutUser() {
        FirebaseManager.logout()
        SessionManager.setKeepSession(requireContext(), false)
        Toast.makeText(requireContext(), getString(R.string.profile_logout_message), Toast.LENGTH_SHORT)
            .show()
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }
}
