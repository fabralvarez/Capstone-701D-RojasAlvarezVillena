package com.example.vitalarmapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build  // <- IMPORT FALTANTE
import android.util.Log
import com.example.vitalarmapp.receiver.AlarmReceiver
import java.util.Calendar

object AlarmManagerHelper {

    fun scheduleAlarm(
        context: Context,
        medicationId: String,
        medicationName: String,
        personName: String,
        dosage: String,
        alarmTime: String, // Formato "HH:mm"
        requestCode: Int
    ) {
        Log.d("AlarmManager", "⏰ Programando alarma: $medicationName a las $alarmTime")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicationName", medicationName)
            putExtra("personName", personName)
            putExtra("dosage", dosage)
            putExtra("medicationId", medicationId)
            action = "ACTION_TRIGGER_ALARM"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Convertir hora string a Calendar
        val alarmCalendar = getCalendarFromTime(alarmTime)

        // Programar alarma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmCalendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmCalendar.timeInMillis,
                pendingIntent
            )
        }

        Log.d("AlarmManager", "✅ Alarma programada: $medicationName a las $alarmTime (ID: $requestCode)")
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AlarmManager", "❌ Alarma cancelada: ID $requestCode")
    }

    private fun getCalendarFromTime(time: String): Calendar {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    fun scheduleAllAlarmsForMedication(
        context: Context,
        medicationId: String,
        medicationName: String,
        personName: String,
        dosage: String,
        alarmTimes: List<String>
    ) {
        Log.d("AlarmManager", "📅 Programando ${alarmTimes.size} alarmas para: $medicationName")

        // Cancelar alarmas existentes para este medicamento
        cancelAllAlarmsForMedication(context, medicationId)

        // Programar nuevas alarmas
        alarmTimes.forEachIndexed { index, time ->
            val requestCode = generateRequestCode(medicationId, index)
            scheduleAlarm(context, medicationId, medicationName, personName, dosage, time, requestCode)
        }

        Log.d("AlarmManager", "✅ ${alarmTimes.size} alarmas programadas para: $medicationName")
    }

    fun cancelAllAlarmsForMedication(context: Context, medicationId: String) {
        // Cancelar hasta 10 alarmas por medicamento (por si hay múltiples horarios)
        for (i in 0..9) {
            val requestCode = generateRequestCode(medicationId, i)
            cancelAlarm(context, requestCode)
        }
        Log.d("AlarmManager", "🗑️ Todas las alarmas canceladas para: $medicationId")
    }

    private fun generateRequestCode(medicationId: String, index: Int): Int {
        return (medicationId.hashCode() and 0xffff) or (index shl 16)
    }
}