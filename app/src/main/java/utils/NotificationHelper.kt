package com.example.vitalarmapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

object NotificationHelper {
    const val CHANNEL_ID = "vitalarm_alarm_channel"
    const val CHANNEL_NAME = "Recordatorios de Medicamentos"
    const val CHANNEL_DESCRIPTION = "Notificaciones para recordar tomar medicamentos"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("NotificationHelper", "📱 Creando canal de notificaciones")

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("NotificationHelper", "✅ Canal de notificaciones creado")
        }
    }
}