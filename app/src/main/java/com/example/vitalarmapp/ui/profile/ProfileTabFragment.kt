package com.example.vitalarmapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vitalarmapp.EditProfileActivity
import com.example.vitalarmapp.MainActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.SettingsActivity
import com.example.vitalarmapp.databinding.FragmentProfileTabBinding
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class ProfileTabFragment : Fragment() {

    private var _binding: FragmentProfileTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.btnProfileLogout.setOnClickListener { showLogoutDialog() }
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    startActivity(SettingsActivity.intent(requireContext()))
                    true
                }

                else -> false
            }
        }
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val profile = withContext(Dispatchers.IO) { FirebaseManager.getCurrentUserProfile() }
            val name = profile?.name?.ifBlank { null }
                ?: getString(R.string.home_greeting_fallback)
            val rut = profile?.rut?.ifBlank { null }
                ?: getString(R.string.profile_rut_placeholder)
            binding.tvProfileName.text = name
            binding.tvProfileRut.text = rut
            renderQrCode(name, rut)
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog
        )
            .setTitle(getString(R.string.profile_logout_dialog_title))
            .setMessage(getString(R.string.profile_logout_dialog_supporting))
            .setNegativeButton(getString(R.string.profile_logout_dialog_confirm)) { _, _ ->
                logoutUser()
            }
            .setPositiveButton(getString(R.string.profile_logout_dialog_cancel), null)
            .show()
    }

    private fun logoutUser() {
        FirebaseManager.logout()
        SessionManager.setKeepSession(requireContext(), false)
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    private fun renderQrCode(name: String, rut: String) {
        try {
            val size = resources.getDimensionPixelSize(R.dimen.profile_qr_size)
            val qrContent = "Nombre: $name\nRUT: $rut"
            val matrix = QRCodeWriter().encode(qrContent, BarcodeFormat.QR_CODE, size, size)
            val bitmap = createBitmap(size, size)
            val backgroundColor = resolveThemeColor(com.google.android.material.R.attr.colorSurface)
            val foregroundColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurface)
            bitmap.eraseColor(backgroundColor)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (matrix[x, y]) foregroundColor else backgroundColor
                }
            }
            binding.imgProfileQr.setImageBitmap(bitmap)
        } catch (_: Exception) {
            Snackbar.make(binding.root, getString(R.string.profile_qr_error), Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.btnProfileLogout)
                .show()
        }
    }

    private fun resolveThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
