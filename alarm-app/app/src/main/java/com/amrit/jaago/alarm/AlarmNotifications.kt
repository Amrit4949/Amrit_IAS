package com.amrit.jaago.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.amrit.jaago.R

object AlarmNotifications {

    /**
     * A new channel id. Channel settings are frozen the first time a channel is
     * created, and the first version of the app shipped a ringing notification
     * that suppressed its own full screen intent. Starting a fresh channel makes
     * sure every phone gets the high-importance, lock-screen-visible settings.
     */
    const val CHANNEL_ALARM = "jaago_alarm_v2"
    const val CHANNEL_INFO = "jaago_info"
    private const val OLD_CHANNEL_RING = "jaago_ring"

    const val NOTIF_RING = 1001
    const val NOTIF_MISSED = 1002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        runCatching { nm.deleteNotificationChannel(OLD_CHANNEL_RING) }

        // No sound and no vibration on the channel itself: AlarmPlayer makes the
        // noise on the alarm stream. The notification is NOT marked silent,
        // because a silent notification is not allowed to launch its full screen
        // intent or to pop up as a banner.
        val alarm = NotificationChannel(
            CHANNEL_ALARM,
            context.getString(R.string.channel_ring_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_ring_desc)
            setSound(null, null)
            enableVibration(false)
            enableLights(true)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }

        val info = NotificationChannel(
            CHANNEL_INFO,
            context.getString(R.string.channel_info_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_info_desc)
            setSound(null, null)
        }

        nm.createNotificationChannel(alarm)
        nm.createNotificationChannel(info)
    }
}
