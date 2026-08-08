package com.amrit.beacon.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.amrit.beacon.MainActivity
import com.amrit.beacon.R
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.ui.AlertActivity

/**
 * The two notifications this app posts, and the channels behind them.
 *
 * They are deliberately very different animals. The listener notification is the price of
 * staying reachable and should be as close to invisible as the platform allows. The alert
 * notification is the opposite: it carries the full-screen intent that lights up the screen
 * of a phone lying face-down on a table.
 */
object Notifications {

    const val CHANNEL_LISTENING = "beacon.listening"
    const val CHANNEL_ALERT = "beacon.alert"

    const val ID_LISTENING = 1001
    const val ID_ALERT = 1002

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val listening = NotificationChannel(
            CHANNEL_LISTENING,
            context.getString(R.string.channel_listening_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.channel_listening_description)
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }

        val alert = NotificationChannel(
            CHANNEL_ALERT,
            context.getString(R.string.channel_alert_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_alert_description)
            // No channel sound or vibration: AlertEngine drives both itself, on the alarm
            // stream. Letting the channel add its own would layer a second, quieter sound
            // underneath the siren and fight it for the speaker.
            setSound(null, null)
            enableVibration(false)
            setBypassDnd(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(listening)
        manager.createNotificationChannel(alert)
    }

    /** The quiet, ongoing "this phone can be found" notification. */
    fun listening(context: Context, peerCount: Int, paired: Boolean) =
        NotificationCompat.Builder(context, CHANNEL_LISTENING)
            .setSmallIcon(R.drawable.ic_beacon)
            .setContentTitle(context.getString(R.string.notification_listening_title))
            .setContentText(
                when {
                    !paired -> context.getString(R.string.notification_listening_unpaired)
                    peerCount == 0 -> context.getString(R.string.notification_listening_alone)
                    else -> context.resources.getQuantityString(
                        R.plurals.notification_listening_peers, peerCount, peerCount
                    )
                }
            )
            .setContentIntent(openApp(context))
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    /**
     * The alert notification. `setFullScreenIntent` is what turns the screen on and launches
     * [AlertActivity] over the lock screen; the notification itself is the fallback for
     * devices and Android versions where that is downgraded to a heads-up banner.
     */
    fun alert(context: Context, state: AlertEngine.State) =
        NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_beacon)
            .setContentTitle(context.getString(R.string.notification_alert_title))
            .setContentText(
                state.sourceName?.let { context.getString(R.string.notification_alert_from, it) }
                    ?: context.getString(R.string.notification_alert_generic)
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenAlert(context), true)
            .addAction(
                0,
                context.getString(R.string.action_stop),
                stopAlert(context),
            )
            .build()

    private fun openApp(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun fullScreenAlert(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            1,
            Intent(context, AlertActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun stopAlert(context: Context): PendingIntent =
        PendingIntent.getService(
            context,
            2,
            Intent(context, BeaconService::class.java).setAction(BeaconService.ACTION_STOP_ALERT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
