package com.example.vitalarmapp.models

data class AlarmRecord(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val medicationName: String = "",
    val medicationDetail: String = "",
    val soundTitle: String = "",
    val soundUri: String = "",
    val date: String = "",
    val time: String = "",
    val scheduledAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null,
    val verifiedAt: Long? = null,
)
