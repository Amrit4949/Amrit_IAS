package com.amrit.jaago.ui

import android.content.Context
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.SoundProfile
import java.util.Calendar

/**
 * One-off alarms a few minutes out, for naps and for proving the alarm reaches
 * a locked screen. Rounded up to the next whole minute, so the stored hour and
 * minute are exactly when it rings and re-arming after a reboot is exact.
 */
object QuickAlarms {

    fun targetFor(minutesFromNow: Int, now: Long = System.currentTimeMillis()): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = now + minutesFromNow * 60_000L
            if (get(Calendar.SECOND) > 0 || get(Calendar.MILLISECOND) > 0) add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    fun create(
        context: Context,
        minutesFromNow: Int,
        label: String = "Quick alarm",
        sound: SoundProfile = SoundProfile.SCREECH,
        volumePercent: Int = 100,
        vibrate: Boolean = true
    ): Alarm {
        val target = targetFor(minutesFromNow)
        val alarm = Alarm(
            id = AlarmStore.newId(),
            hour = target.get(Calendar.HOUR_OF_DAY),
            minute = target.get(Calendar.MINUTE),
            label = label,
            enabled = true,
            days = emptySet(),
            sound = sound,
            vibrate = vibrate,
            volumePercent = volumePercent,
            extraLoud = true,
            isQuick = true,
            challenges = emptyList(),
            autoSilenceMinutes = 10
        )
        AlarmStore.upsert(alarm)
        AlarmScheduler.schedule(context, alarm)
        return alarm
    }
}
