package com.example.vitalarmapp.models

data class User(
    val id: String = "",
    val name: String = "",
    val rut: String = "",
    val email: String = "",
    val createdAt: Long = 0
)