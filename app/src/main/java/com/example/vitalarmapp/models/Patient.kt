package com.example.vitalarmapp.models

data class Patient(
    val id: String = "",
    val name: String = "",
    val birthDate: String? = null,
    val userId: String = "",
    val createdAt: Long = 0
)