package com.example.vitalarmapp.notifications

import android.content.Context
import androidx.annotation.VisibleForTesting
import org.json.JSONArray
import org.json.JSONObject

internal data class NotificationEntry(
    val title: String,
    val detail: String,
    val timestamp: Long,
)

internal class NotificationHistoryRepository {

    fun loadEntries(context: Context): List<NotificationEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawEntries = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()

        val parsedEntries = runCatching {
            val array = JSONArray(rawEntries)
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.toEntry()
            }
        }.getOrElse { emptyList() }

        return parsedEntries.sortedByDescending { it.timestamp }
    }

    fun addEntry(context: Context, entry: NotificationEntry) {
        val existing = loadEntries(context).toMutableList()
        existing.add(0, entry)

        val limited = existing
            .sortedByDescending { it.timestamp }
            .take(MAX_ENTRIES)

        saveEntries(context, limited)
    }

    @VisibleForTesting
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ENTRIES)
            .apply()
    }

    private fun saveEntries(context: Context, entries: List<NotificationEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put(KEY_TITLE, entry.title)
                    .put(KEY_DETAIL, entry.detail)
                    .put(KEY_TIMESTAMP, entry.timestamp)
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENTRIES, array.toString())
            .apply()
    }

    private fun JSONObject.toEntry(): NotificationEntry? {
        val title = optString(KEY_TITLE)
        val detail = optString(KEY_DETAIL)
        val timestamp = optLong(KEY_TIMESTAMP)

        if (title.isBlank() || detail.isBlank() || timestamp <= 0) return null

        return NotificationEntry(
            title = title,
            detail = detail,
            timestamp = timestamp,
        )
    }

    private companion object {
        private const val PREFS_NAME = "vitalarm_notifications"
        private const val KEY_ENTRIES = "entries"
        private const val KEY_TITLE = "title"
        private const val KEY_DETAIL = "detail"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val MAX_ENTRIES = 10
    }
}
