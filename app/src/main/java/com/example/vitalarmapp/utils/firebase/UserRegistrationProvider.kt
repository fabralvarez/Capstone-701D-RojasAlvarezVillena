package com.example.vitalarmapp.utils.firebase

interface UserRegistrationProvider {
    suspend fun registerUser(name: String, rut: String, email: String, password: String): RegistrationResult
    fun logout()
}
