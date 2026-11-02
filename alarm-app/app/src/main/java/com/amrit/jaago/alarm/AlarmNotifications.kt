package com.amrit.jaago.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object AlarmNotifications {

    const val CHANNEL_RING = "jaago_ring"
    const val CHANNEL_INFO = "jaago_info"

    const val NOTIF_RING = 1001
    const val NOTIF_MISSED = 1002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // The ringing channel is silent on purpose: AlarmPlayer makes the noise, not
        // the notification, so the sound cannot be muted by lowering the ring volume.
        val ring = NotificationChannel(
            CHANNEL_RING,
            context.getString(com.amrit.jaago.R.string.channel_ring_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(com.amrit.jaago.R.string.channel_ring_desc)
            setSound(null, null)
            enableVibration(false)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }

        val info = NotificationChannel(
            CHANNEL_INFO,
            context.getString(com.amrit.jaago.R.string.channel_info_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(com.amrit.jaago.R.string.channel_info_desc)
            setSound(null, null)
        }

        nm.createNotificationChannel(ring)
        nm.createNotificationChannel(info)
    }
}
