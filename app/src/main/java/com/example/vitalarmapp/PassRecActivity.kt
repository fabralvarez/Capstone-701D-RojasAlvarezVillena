package com.example.vitalarmapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalarmapp.databinding.ActivityPassRecBinding

class PassRecActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassRecBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassRecBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
    }
}