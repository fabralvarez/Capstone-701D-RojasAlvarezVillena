package com.example.vitalarmapp.utils.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import androidx.core.content.edit

data class AlarmRecord(
    val id: String = UUID.randomUUID().toString(),
    val patientName: String,
    val medicationName: String,
    val medicationDetail: String,
    val time: String,
    val scheduledAt: Long,
    val triggeredAt: Long? = null,
    val photoPath: String? = null,
)

class AlarmRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun save(record: AlarmRecord): AlarmRecord {
        val updated = currentRecords().toMutableList().apply {
            add(record)
        }
        persist(updated)
        return record
    }

    fun markTriggered(id: String, timestamp: Long = System.currentTimeMillis()) {
        update(id) { it.copy(triggeredAt = it.triggeredAt ?: timestamp) }
    }

    fun markVerified(id: String, photoPath: String, timestamp: Long = System.currentTimeMillis()) {
        update(id) { record ->
            record.copy(
                triggeredAt = record.triggeredAt ?: timestamp,
                photoPath = photoPath
            )
        }
        pruneHistory()
    }

    fun get(id: String): AlarmRecord? = currentRecords().firstOrNull { it.id == id }

    fun getHistory(daysBack: Long = MAX_HISTORY_DAYS): List<AlarmRecord> {
        val cutoff = Instant.now()
            .minusSeconds(daysBack * 86_400)
            .toEpochMilli()

        return currentRecords()
            .filter { it.photoPath != null && (it.triggeredAt ?: it.scheduledAt) >= cutoff }
            .sortedByDescending { it.triggeredAt ?: it.scheduledAt }
    }

    private fun update(id: String, transform: (AlarmRecord) -> AlarmRecord) {
        val updated = currentRecords().map { record ->
            if (record.id == id) transform(record) else record
        }
        persist(updated)
    }

    private fun pruneHistory(daysBack: Long = MAX_HISTORY_DAYS) {
        val cutoff = LocalDateTime.now()
            .minusDays(daysBack)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val trimmed = currentRecords().filter { record ->
            val pivot = record.triggeredAt ?: record.scheduledAt
            pivot >= cutoff
        }
        persist(trimmed)
    }

    private fun currentRecords(): List<AlarmRecord> {
        val json = prefs.getString(KEY_ALARMS, "[]")
        val type = object : TypeToken<List<AlarmRecord>>() {}.type
        return runCatching { gson.fromJson<List<AlarmRecord>>(json, type) }
            .getOrDefault(emptyList())
    }

    private fun persist(records: List<AlarmRecord>) {
        prefs.edit { putString(KEY_ALARMS, gson.toJson(records)) }
    }

    companion object {
        private const val PREFS_NAME = "alarms_repository"
        private const val KEY_ALARMS = "alarms_records"
        private const val MAX_HISTORY_DAYS = 7L
    }
}
