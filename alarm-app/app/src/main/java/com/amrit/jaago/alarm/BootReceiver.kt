package com.amrit.jaago.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amrit.jaago.model.AlarmStore

/**
 * AlarmManager forgets everything on reboot, and also when the clock or the time
 * zone changes, so every alarm is armed again here.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                AlarmStore.init(context)
                AlarmNotifications.createChannels(context)
                AlarmScheduler.rescheduleAll(context)
            }
        }
    }
}
