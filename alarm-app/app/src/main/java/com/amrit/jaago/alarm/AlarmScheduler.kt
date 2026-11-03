package com.amrit.jaago.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.amrit.jaago.MainActivity
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_ALARM_ID = "com.amrit.jaago.ALARM_ID"
    const val EXTRA_SNOOZED = "com.amrit.jaago.SNOOZED"
    const val EXTRA_TEST = "com.amrit.jaago.TEST"

    private fun manager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * One PendingIntent per alarm. Snooze and test reuse it, so re-arming always
     * replaces whatever was pending rather than stacking a second trigger.
     */
    private fun firePendingIntent(
        context: Context,
        alarmId: Int,
        snoozed: Boolean,
        test: Boolean
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.amrit.jaago.FIRE"
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_SNOOZED, snoozed)
            putExtra(EXTRA_TEST, test)
            data = Uri.parse("jaago://alarm/$alarmId")
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

    fun canScheduleExact(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager(context).canScheduleExactAlarms()
        } else {
            true
        }

    /** The next moment this alarm should go off by its own schedule. */
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
        base.add(Calendar.DAY_OF_YEAR, 1)
        return base.timeInMillis
    }

    /** When this alarm will actually ring next: a pending snooze or test wins. */
    fun effectiveNext(alarm: Alarm, from: Long = System.currentTimeMillis()): Long? {
        val override = AlarmStore.overrideAt(alarm.id)
        if (override > from) return override
        return if (alarm.enabled) nextTrigger(alarm, from) else null
    }

    /**
     * Arms the alarm. A snooze or test that is still pending is kept, so editing
     * an alarm, rebooting, or the process restarting never throws one away.
     */
    fun schedule(context: Context, alarm: Alarm): Long {
        val now = System.currentTimeMillis()
        if (!alarm.enabled) {
            cancel(context, alarm.id)
            AlarmStore.clearOverride(alarm.id)
            return 0L
        }
        val override = AlarmStore.overrideAt(alarm.id)
        if (override > now) {
            armAt(context, alarm.id, override, snoozed = true, test = false)
            return override
        }
        AlarmStore.clearOverride(alarm.id)
        val at = nextTrigger(alarm, now)
        armAt(context, alarm.id, at, snoozed = false, test = false)
        return at
    }

    fun scheduleSnooze(context: Context, alarm: Alarm, minutes: Int): Long {
        val at = System.currentTimeMillis() + minutes * 60_000L
        AlarmStore.setOverride(alarm.id, at)
        armAt(context, alarm.id, at, snoozed = true, test = false)
        return at
    }

    /**
     * Rings this alarm for real after [delayMs], through AlarmManager and the
     * receiver, exactly like a scheduled morning. The only honest way to check
     * that it will appear on a locked screen.
     */
    fun scheduleTest(context: Context, alarm: Alarm, delayMs: Long = 60_000L): Long {
        val at = System.currentTimeMillis() + delayMs
        AlarmStore.setOverride(alarm.id, at)
        armAt(context, alarm.id, at, snoozed = true, test = true)
        return at
    }

    private fun armAt(context: Context, alarmId: Int, at: Long, snoozed: Boolean, test: Boolean) {
        val am = manager(context)
        val operation = firePendingIntent(context, alarmId, snoozed, test)
        try {
            if (canScheduleExact(context)) {
                // Survives Doze, shows the alarm icon, and gives the app a short
                // allow-list window so the foreground service may start.
                am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPendingIntent(context)), operation)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
            }
        } catch (se: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
        }
    }

    fun cancel(context: Context, alarmId: Int) {
        manager(context).cancel(firePendingIntent(context, alarmId, snoozed = false, test = false))
    }

    /** Re-arms every alarm. Used after boot, an app update and clock changes. */
    fun rescheduleAll(context: Context) {
        val now = System.currentTimeMillis()
        AlarmStore.all().forEach { alarm ->
            val override = AlarmStore.overrideAt(alarm.id)
            when {
                override > now -> armAt(context, alarm.id, override, snoozed = true, test = false)
                alarm.enabled -> schedule(context, alarm)
                else -> {
                    cancel(context, alarm.id)
                    AlarmStore.clearOverride(alarm.id)
                }
            }
        }
    }

    /** "in 7h 20m", compact, for small pills. */
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

    /** "Rings in 22 hr 37 min", the way Alarmy phrases it. */
    fun ringsInText(at: Long, from: Long = System.currentTimeMillis()): String {
        val totalSeconds = ((at - from).coerceAtLeast(0L)) / 1000L
        if (totalSeconds < 60) return "Rings in less than a minute"
        var minutes = (totalSeconds + 59) / 60
        val days = minutes / (24 * 60)
        minutes %= (24 * 60)
        val hours = minutes / 60
        val mins = minutes % 60
        val parts = mutableListOf<String>()
        if (days > 0) parts.add(if (days == 1L) "1 day" else "$days days")
        if (hours > 0) parts.add("$hours hr")
        if (mins > 0 && days == 0L) parts.add("$mins min")
        return "Rings in " + parts.joinToString(" ")
    }
}
