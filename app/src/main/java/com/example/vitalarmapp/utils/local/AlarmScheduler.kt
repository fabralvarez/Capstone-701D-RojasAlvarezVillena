package com.example.vitalarmapp.utils.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.vitalarmapp.AlarmRingingActivity
import com.example.vitalarmapp.models.AlarmRecord
import java.util.concurrent.TimeUnit

class AlarmScheduler(private val context: Context) {

    fun schedule(record: AlarmRecord) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putAlarmPayload(record)
        }

        val receiverPendingIntent = PendingIntent.getBroadcast(
            context,
            record.id.hashCode(),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putAlarmPayload(record)
        }
        val alarmInfo = AlarmManager.AlarmClockInfo(
            record.scheduledAt,
            PendingIntent.getActivity(
                context,
                record.id.hashCode(),
                displayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        try {
            alarmManager.setAlarmClock(alarmInfo, receiverPendingIntent)
            schedulePreAlarmNotification(alarmManager, record)
        } catch (_: SecurityException) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private fun schedulePreAlarmNotification(alarmManager: AlarmManager, record: AlarmRecord) {
        val reminderAt = record.scheduledAt - TimeUnit.MINUTES.toMillis(PRE_NOTIFICATION_MINUTES)
        val triggerAt = if (reminderAt > System.currentTimeMillis()) {
            reminderAt
        } else {
            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5)
        }

        val reminderIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_PRE_ALARM_NOTIFICATION
            putAlarmPayload(record)
        }

        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            record.id.hashCode() + PRE_NOTIFICATION_REQUEST_CODE_OFFSET,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            reminderPendingIntent
        )
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "ACTION_TRIGGER_ALARM"
        const val ACTION_PRE_ALARM_NOTIFICATION = "ACTION_PRE_ALARM_NOTIFICATION"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        private const val PRE_NOTIFICATION_MINUTES = 30L
        private const val PRE_NOTIFICATION_REQUEST_CODE_OFFSET = 42_000

        private const val EXTRA_PATIENT_NAME = "extra_patient_name"
        private const val EXTRA_PATIENT_ID = "extra_patient_id"
        private const val EXTRA_MED_NAME = "extra_med_name"
        private const val EXTRA_MED_DETAIL = "extra_med_detail"
        private const val EXTRA_DATE = "extra_alarm_date"
        private const val EXTRA_TIME = "extra_alarm_time"
        private const val EXTRA_SCHEDULED_AT = "extra_alarm_scheduled_at"

        fun Intent.putAlarmPayload(record: AlarmRecord) {
            putExtra(EXTRA_ALARM_ID, record.id)
            putExtra(EXTRA_PATIENT_ID, record.patientId)
            putExtra(EXTRA_PATIENT_NAME, record.patientName)
            putExtra(EXTRA_MED_NAME, record.medicationName)
            putExtra(EXTRA_MED_DETAIL, record.medicationDetail)
            putExtra(EXTRA_DATE, record.date)
            putExtra(EXTRA_TIME, record.time)
            putExtra(EXTRA_SCHEDULED_AT, record.scheduledAt)
        }

        fun Intent.extractAlarmPayload(): AlarmRecord? {
            val id = getStringExtra(EXTRA_ALARM_ID) ?: return null
            val patientId = getStringExtra(EXTRA_PATIENT_ID) ?: return null
            val patientName = getStringExtra(EXTRA_PATIENT_NAME) ?: return null
            val medName = getStringExtra(EXTRA_MED_NAME) ?: return null
            val medDetail = getStringExtra(EXTRA_MED_DETAIL) ?: return null
            val date = getStringExtra(EXTRA_DATE) ?: return null
            val time = getStringExtra(EXTRA_TIME) ?: return null
            val scheduledAt = getLongExtra(EXTRA_SCHEDULED_AT, 0L)
            if (scheduledAt == 0L) return null

            return AlarmRecord(
                id = id,
                patientId = patientId,
                patientName = patientName,
                medicationName = medName,
                medicationDetail = medDetail,
                date = date,
                time = time,
                scheduledAt = scheduledAt,
            )
        }
    }
}
