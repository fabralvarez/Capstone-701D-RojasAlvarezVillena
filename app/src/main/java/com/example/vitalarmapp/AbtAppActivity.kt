package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityAbtAppBinding

class AbtAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbtAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbtAppBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupAppBar()
        binding.tvVersion.text = getString(R.string.about_app_version, BuildConfig.VERSION_NAME)
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
