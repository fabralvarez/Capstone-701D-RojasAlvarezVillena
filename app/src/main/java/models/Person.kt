package models

data class Person(
    val id: String = "",
    val name: String = "",
    val birthDate: String? = null,
    val userId: String = "",
    val createdAt: Long = 0
)