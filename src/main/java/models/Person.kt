package com.example.vitalarmapp.models

data class Person(
    val id: String = "",
    val name: String = "",
    val birthDate: String? = null,
    val userId: String = "",
    val createdAt: Long = 0
) {

    data class Contacto(
        val nombre: String,
        val relacion: String,
        val numero: String
    )
}