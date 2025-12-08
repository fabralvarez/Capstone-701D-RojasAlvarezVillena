package com.example.vitalarmapp.utils.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.vitalarmapp.AlarmRingingActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    fun schedule(record: AlarmRecord) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = computeTriggerMillis(record.time)

        val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, record.id)
        }

        val receiverPendingIntent = PendingIntent.getBroadcast(
            context,
            record.id.hashCode(),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayIntent = Intent(context, AlarmRingingActivity::class.java)
        val alarmInfo = AlarmManager.AlarmClockInfo(
            triggerAt, PendingIntent.getActivity(
                context,
                record.id.hashCode(),
                displayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        try {
            alarmManager.setAlarmClock(alarmInfo, receiverPendingIntent)
        } catch (_: SecurityException) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun computeTriggerMillis(time: String, now: LocalDateTime = LocalDateTime.now()): Long {
            val parsedTime = LocalTime.parse(time)
            val today = LocalDate.from(now)
            val targetDate =
                if (parsedTime.isBefore(now.toLocalTime())) today.plusDays(1) else today
            return LocalDateTime.of(targetDate, parsedTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    }
}
