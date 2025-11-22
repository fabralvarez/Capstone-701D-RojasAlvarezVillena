package com.example.vitalarmapp.utils.validation

interface RutValidationService {
    suspend fun isValid(rut: String): Boolean
}
