package com.example.vitalarmapp.utils.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vitalarmapp.AlarmRingingActivity
import com.example.vitalarmapp.utils.local.AlarmRepository
import com.example.vitalarmapp.utils.local.AlarmScheduler.Companion.EXTRA_ALARM_ID


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "🔔 Alarma recibida!")

        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
        if (alarmId.isNullOrEmpty()) return

        val repository = AlarmRepository(context)
        repository.markTriggered(alarmId)

        val launchIntent = AlarmRingingActivity.intent(context, alarmId)
        context.startActivity(launchIntent)
    }
}