package com.example.vitalarmapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import com.example.vitalarmapp.databinding.FragmentHomeTabBinding
import com.google.android.material.transition.MaterialFadeThrough

class HomeTabFragment : Fragment() {

    private var _binding: FragmentHomeTabBinding? = null
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
        _binding = FragmentHomeTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserNameLoading(true)
        refreshContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshContent() {
        if (view == null) return
    }

    fun setUserNameLoading(isLoading: Boolean) {
        if (view == null) return
        binding.homeLoadingIndicator.isVisible = isLoading
        binding.homeContent.isVisible = !isLoading
    }

    fun onUserNameLoaded() {
        setUserNameLoading(false)
        refreshContent()
    }
}
