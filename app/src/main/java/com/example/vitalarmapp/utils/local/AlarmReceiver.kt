package com.example.vitalarmapp.utils.local

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vitalarmapp.AlarmRingingActivity
import com.example.vitalarmapp.LanMenuActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.models.AlarmRecord
import com.example.vitalarmapp.notifications.NotificationEntry
import com.example.vitalarmapp.notifications.NotificationHistoryRepository
import com.example.vitalarmapp.utils.firebase.FirebaseManager
import com.example.vitalarmapp.utils.local.AlarmScheduler.Companion.ACTION_PRE_ALARM_NOTIFICATION
import com.example.vitalarmapp.utils.local.AlarmScheduler.Companion.ACTION_TRIGGER_ALARM
import com.example.vitalarmapp.utils.local.AlarmScheduler.Companion.extractAlarmPayload
import com.example.vitalarmapp.utils.local.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PRE_ALARM_NOTIFICATION -> handlePreAlarmNotification(context, intent)
            ACTION_TRIGGER_ALARM -> handleAlarmTrigger(context, intent)
            else -> Log.d("AlarmReceiver", "Intent ignorado: ${intent.action}")
        }
    }

    private fun handlePreAlarmNotification(context: Context, intent: Intent) {
        val payload = intent.extractAlarmPayload() ?: return
        NotificationHelper.createNotificationChannel(context)

        val contentText = context.getString(
            R.string.pre_alarm_notification_content,
            payload.medicationName,
            payload.patientName,
            payload.time
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.id.hashCode() + PRE_NOTIFICATION_REQUEST_CODE_OFFSET,
            Intent(context, LanMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.pre_alarm_notification_title))
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(
                        R.string.pre_alarm_notification_detail,
                        payload.medicationName,
                        payload.medicationDetail,
                        payload.patientName,
                        payload.date,
                        payload.time
                    )
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            payload.id.hashCode() + PRE_NOTIFICATION_REQUEST_CODE_OFFSET,
            notification
        )

        NotificationHistoryRepository().addEntry(
            context,
            NotificationEntry(
                title = context.getString(R.string.pre_alarm_notification_title),
                detail = context.getString(
                    R.string.pre_alarm_notification_detail,
                    payload.medicationName,
                    payload.medicationDetail,
                    payload.patientName,
                    payload.date,
                    payload.time
                ),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "🔔 Alarma recibida!")

        val payload = intent.extractAlarmPayload() ?: return
        markTriggered(payload)

        val launchIntent = AlarmRingingActivity.intent(context, payload)
        context.startActivity(launchIntent)
    }

    private fun markTriggered(record: AlarmRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseManager.markAlarmTriggered(record.id)
        }
    }

    companion object {
        private const val PRE_NOTIFICATION_REQUEST_CODE_OFFSET = 84_000
    }
}
