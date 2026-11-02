package com.amrit.jaago.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.amrit.jaago.model.AlarmStore

/** Fired by AlarmManager at the alarm time. Starts the service, then re-arms. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AlarmStore.init(context)
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val wasSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZED, false)
        val alarm = AlarmStore.get(alarmId)
        if (alarm == null) {
            Log.w(TAG, "Fired for unknown alarm $alarmId")
            return
        }

        AlarmService.ring(context, alarmId)

        // Re-arm straight away so tomorrow is covered even if this one is never solved.
        if (alarm.days.isNotEmpty()) {
            AlarmScheduler.schedule(context, alarm)
        } else if (!wasSnooze) {
            // A one-shot alarm is done once it has rung.
            AlarmStore.upsert(alarm.copy(enabled = false))
            AlarmScheduler.cancel(context, alarm.id)
        }
    }

    companion object {
        private const val TAG = "JaagoReceiver"
    }
}
