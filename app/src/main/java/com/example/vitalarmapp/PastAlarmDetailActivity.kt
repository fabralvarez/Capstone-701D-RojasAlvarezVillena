package com.example.vitalarmapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PastAlarmDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_alarm_detail)

        if (savedInstanceState == null) {
            val alarmId = intent.getStringExtra(EXTRA_ALARM_ID).orEmpty()
            supportFragmentManager.beginTransaction()
                .replace(R.id.past_alarm_detail_container, PastAlarmDetailFragment.newInstance(alarmId))
                .commit()
        }
    }

    companion object {
        private const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun intent(context: android.content.Context, id: String) =
            android.content.Intent(context, PastAlarmDetailActivity::class.java).apply {
                putExtra(EXTRA_ALARM_ID, id)
            }
    }
}
