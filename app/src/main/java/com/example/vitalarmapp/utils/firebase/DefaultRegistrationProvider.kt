package com.example.vitalarmapp.utils.firebase

class DefaultRegistrationProvider : UserRegistrationProvider {
    override suspend fun registerUser(
        name: String,
        rut: String,
        email: String,
        password: String
    ): RegistrationResult {
        return FirebaseManager.registerUser(name, rut, email, password)
    }

    override fun logout() {
        FirebaseManager.logout()
    }
}
