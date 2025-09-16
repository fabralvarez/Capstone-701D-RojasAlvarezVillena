package com.example.vitalarmapp.models

data class Medication(
    val id: String,
    val nombre: String,
    val gramaje: String,
    val horaAdministrar: String,
    val personaAdministrar: String,
    val codigoBarras: String,
    val imagen: Int
)