package com.example.vitalarmapp.models

data class Person(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val fechaNacimiento: String,
    val contacto: Contacto,
    val prescripcionMedica: String
) {
    data class Contacto(
        val nombre: String,
        val relacion: String,
        val numero: String
    )
}