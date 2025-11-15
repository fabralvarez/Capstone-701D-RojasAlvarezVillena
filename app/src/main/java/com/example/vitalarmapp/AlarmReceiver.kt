package com.example.vitalarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vitalarmapp.services.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "🔔 Alarma recibida!")

        val medicationName = intent.getStringExtra("medicationName")
        val personName = intent.getStringExtra("personName")
        val dosage = intent.getStringExtra("dosage")
        val medicationId = intent.getStringExtra("medicationId")

        Log.d("AlarmReceiver", "💊 Mostrando notificación para: $medicationName - $personName")

        // Iniciar el servicio que mostrará la notificación
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("medicationName", medicationName)
            putExtra("personName", personName)
            putExtra("dosage", dosage)
            putExtra("medicationId", medicationId)
        }
        context.startService(serviceIntent)
    }
}