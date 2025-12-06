package com.example.vitalarmapp.models

data class Patient(
    val id: String = "",
    val name: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val notes: String = "",
    val userId: String = "",
    val userName: String = "",
    val createdAt: Long = 0
)
