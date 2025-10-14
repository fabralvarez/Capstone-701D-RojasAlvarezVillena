package com.example.vitalarmapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.vitalarmapp.MainMenuActivity
import com.example.vitalarmapp.R
import com.example.vitalarmapp.receiver.AlarmReceiver

class AlarmService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "🎯 Iniciando servicio de alarma")

        val medicationName = intent?.getStringExtra("medicationName") ?: "Medicamento"
        val personName = intent?.getStringExtra("personName") ?: "Persona"
        val dosage = intent?.getStringExtra("dosage") ?: "Sin dosis"
        val medicationId = intent?.getStringExtra("medicationId") ?: ""

        // Crear notificación
        showNotification(medicationName, personName, dosage, medicationId)

        // Reproducir sonido de alarma
        playAlarmSound()

        return START_NOT_STICKY
    }

    private fun showNotification(medicationName: String, personName: String, dosage: String, medicationId: String) {
        Log.d("AlarmService", "📱 Creando notificación: $medicationName para $personName")

        // Intent para cuando se hace click en la notificación
        val mainIntent = Intent(this, MainMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para "Marcar como tomado"
        val takenIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "ACTION_MARK_TAKEN"
            putExtra("medicationId", medicationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear notificación con iconos del sistema
        val notification = NotificationCompat.Builder(this, "vitalarm_alarm_channel")
            .setContentTitle("💊 Hora de tomar medicamento")
            .setContentText("$medicationName para $personName")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono del sistema
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("💊 $medicationName\n👤 Para: $personName\n📏 Dosis: $dosage\n\nToca para abrir la app"))
            .addAction(android.R.drawable.ic_input_add, "✅ Tomado", takenPendingIntent) // Icono del sistema
            .build()

        // Mostrar notificación
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicationId.hashCode(), notification)

        Log.d("AlarmService", "✅ Notificación mostrada: ID ${medicationId.hashCode()}")
    }

    private fun playAlarmSound() {
        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmSound == null) {
                // Si no hay alarma, usar notificación
                val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(this, notificationSound)
                ringtone?.play()
            } else {
                val ringtone = RingtoneManager.getRingtone(this, alarmSound)
                ringtone?.play()
            }
            Log.d("AlarmService", "🔊 Sonido de alarma reproducido")
        } catch (e: Exception) {
            Log.e("AlarmService", "❌ Error reproduciendo sonido: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}