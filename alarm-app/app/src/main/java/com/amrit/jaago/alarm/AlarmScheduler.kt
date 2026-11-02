package com.amrit.jaago.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.amrit.jaago.MainActivity
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_ALARM_ID = "com.amrit.jaago.ALARM_ID"
    const val EXTRA_SNOOZED = "com.amrit.jaago.SNOOZED"

    private fun manager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun firePendingIntent(context: Context, alarmId: Int, snoozed: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.amrit.jaago.FIRE"
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_SNOOZED, snoozed)
            // Keeps the PendingIntent unique per alarm even though extras are ignored for equality.
            data = android.net.Uri.parse("jaago://alarm/$alarmId")
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** True when the system lets us fire on an exact minute. */
    fun canScheduleExact(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager(context).canScheduleExactAlarms()
        } else {
            true
        }

    /** The next moment this alarm should go off, in wall clock millis. */
    fun nextTrigger(alarm: Alarm, from: Long = System.currentTimeMillis()): Long {
        val base = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (alarm.days.isEmpty()) {
            if (base.timeInMillis <= from) base.add(Calendar.DAY_OF_YEAR, 1)
            return base.timeInMillis
        }
        for (offset in 0..7) {
            val candidate = base.clone() as Calendar
            candidate.add(Calendar.DAY_OF_YEAR, offset)
            if (candidate.timeInMillis > from &&
                alarm.days.contains(candidate.get(Calendar.DAY_OF_WEEK))
            ) {
                return candidate.timeInMillis
            }
        }
        // Should not happen, but never return something in the past.
        base.add(Calendar.DAY_OF_YEAR, 1)
        return base.timeInMillis
    }

    fun schedule(context: Context, alarm: Alarm): Long {
        cancel(context, alarm.id)
        if (!alarm.enabled) return 0L
        val at = nextTrigger(alarm)
        armAt(context, alarm.id, at, snoozed = false)
        return at
    }

    fun scheduleSnooze(context: Context, alarm: Alarm, minutes: Int): Long {
        cancel(context, alarm.id)
        val at = System.currentTimeMillis() + minutes * 60_000L
        armAt(context, alarm.id, at, snoozed = true)
        return at
    }

    private fun armAt(context: Context, alarmId: Int, at: Long, snoozed: Boolean) {
        val am = manager(context)
        val operation = firePendingIntent(context, alarmId, snoozed)
        try {
            if (canScheduleExact(context)) {
                // setAlarmClock survives Doze, shows the system alarm icon, and gets the
                // app a short allow-list window so the service can start.
                am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPendingIntent(context)), operation)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
            }
        } catch (se: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
        }
    }

    fun cancel(context: Context, alarmId: Int) {
        manager(context).cancel(firePendingIntent(context, alarmId, false))
    }

    /** Re-arms every enabled alarm. Used after boot, after an update and on app start. */
    fun rescheduleAll(context: Context) {
        AlarmStore.all().forEach { alarm ->
            if (alarm.enabled) schedule(context, alarm) else cancel(context, alarm.id)
        }
    }

    /** "in 7 h 20 m" style text for the alarm list. */
    fun countdownText(at: Long, from: Long = System.currentTimeMillis()): String {
        var minutes = ((at - from).coerceAtLeast(0L) + 59_999L) / 60_000L
        val days = minutes / (24 * 60)
        minutes %= (24 * 60)
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            days > 0 -> "in ${days}d ${hours}h"
            hours > 0 -> "in ${hours}h ${mins}m"
            else -> "in ${mins}m"
        }
    }
}
