package com.amrit.jaago.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.amrit.jaago.model.AlarmStore

/** Fired by AlarmManager. Starts the ringing service, then re-arms. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AlarmStore.init(context)
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val wasSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZED, false)
        val wasTest = intent.getBooleanExtra(AlarmScheduler.EXTRA_TEST, false)
        val alarm = AlarmStore.get(alarmId)
        if (alarm == null) {
            Log.w(TAG, "Fired for unknown alarm $alarmId")
            return
        }

        // Whatever one-off time brought us here has now been used.
        AlarmStore.clearOverride(alarmId)

        try {
            AlarmService.ring(context, alarmId)
        } catch (t: Throwable) {
            // Android can refuse a foreground service start if the exact-alarm
            // allow-list window was missed. Never crash the receiver over it.
            Log.e(TAG, "Could not start the alarm service", t)
        }

        when {
            // A snooze or a test borrowed this alarm's trigger. Put the real
            // schedule back: the next day for a repeating alarm, or the still
            // pending time of a one-shot that has not rung for real yet.
            wasSnooze || wasTest -> if (alarm.enabled) AlarmScheduler.schedule(context, alarm)
            // Repeating: arm the next day now, in case this one is never solved.
            alarm.days.isNotEmpty() -> AlarmScheduler.schedule(context, alarm)
            // A one-shot alarm is spent once its real time has rung.
            else -> {
                AlarmStore.upsert(alarm.copy(enabled = false))
                AlarmScheduler.cancel(context, alarm.id)
            }
        }
    }

    companion object {
        private const val TAG = "AriseReceiver"
    }
}
