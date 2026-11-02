package com.amrit.jaago.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.amrit.jaago.R
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.WakeEvent
import com.amrit.jaago.ui.ring.RingActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps making noise until a challenge is solved. Running as a foreground service
 * is what stops Android from killing the alarm half way through.
 */
class AlarmService : Service() {

    private var player: AlarmPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentAlarm: Alarm? = null

    private val autoSilenceTask = Runnable { autoSilence() }
    private val reRaiseTask = object : Runnable {
        override fun run() {
            val alarm = currentAlarm ?: return
            // Re-posting the notification pulls the full screen alarm back to the
            // front if the user wandered off to another app without solving it.
            notify(alarm)
            handler.postDelayed(this, RE_RAISE_INTERVAL_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RING -> startRinging(intent.getIntExtra(EXTRA_ALARM_ID, -1))
            ACTION_SNOOZE -> handleSnooze()
            ACTION_DISMISS -> handleDismiss()
            else -> stopEverything()
        }
        return START_NOT_STICKY
    }

    private fun startRinging(alarmId: Int) {
        val alarm = AlarmStore.get(alarmId)
        if (alarm == null) {
            Log.w(TAG, "Alarm $alarmId no longer exists")
            stopEverything()
            return
        }

        // Must happen within a few seconds of the service starting.
        startForegroundCompat(alarm)

        if (currentAlarm?.id == alarm.id && player != null) return // already ringing

        currentAlarm = alarm
        _ringingAlarmId.value = alarm.id
        _ringingSince.value = System.currentTimeMillis()

        acquireWakeLock(alarm)
        player = AlarmPlayer(this).also { it.start(alarm) }

        handler.removeCallbacks(autoSilenceTask)
        if (alarm.autoSilenceMinutes > 0) {
            handler.postDelayed(autoSilenceTask, alarm.autoSilenceMinutes * 60_000L)
        }
        handler.removeCallbacks(reRaiseTask)
        handler.postDelayed(reRaiseTask, RE_RAISE_INTERVAL_MS)

        launchRingScreen(alarm.id)
    }

    private fun launchRingScreen(alarmId: Int) {
        // The full screen intent on the notification is the reliable path; this direct
        // start is a bonus for when the screen is already on and the app is allowed it.
        try {
            startActivity(ringIntent(this, alarmId))
        } catch (t: Throwable) {
            Log.w(TAG, "Could not start the ring screen directly", t)
        }
    }

    private fun handleSnooze() {
        val alarm = currentAlarm ?: return stopEverything()
        val used = AlarmStore.snoozesUsed(alarm.id)
        if (!alarm.snoozeEnabled || used >= alarm.maxSnoozes) {
            Log.w(TAG, "Snooze refused for alarm ${alarm.id}")
            return
        }
        AlarmStore.noteSnoozeUsed(alarm.id)
        AlarmScheduler.scheduleSnooze(this, alarm, alarm.snoozeMinutes)
        logEvent(alarm, "Snoozed for ${alarm.snoozeMinutes} min")
        stopEverything()
    }

    private fun handleDismiss() {
        val alarm = currentAlarm
        if (alarm != null) {
            AlarmStore.resetSnoozes(alarm.id)
            logEvent(alarm, "Woke up")
        }
        stopEverything()
    }

    private fun autoSilence() {
        val alarm = currentAlarm ?: return
        AlarmStore.resetSnoozes(alarm.id)
        logEvent(alarm, "Missed, silenced automatically")
        postMissedNotice(alarm)
        stopEverything()
    }

    private fun logEvent(alarm: Alarm, outcome: String) {
        val started = _ringingSince.value
        val seconds = if (started > 0) ((System.currentTimeMillis() - started) / 1000L).toInt() else 0
        AlarmStore.logEvent(
            WakeEvent(
                atMillis = if (started > 0) started else System.currentTimeMillis(),
                label = alarm.label.ifBlank { alarm.timeText() },
                outcome = outcome,
                secondsTaken = seconds
            )
        )
    }

    private fun stopEverything() {
        handler.removeCallbacks(autoSilenceTask)
        handler.removeCallbacks(reRaiseTask)
        player?.stop()
        player = null
        currentAlarm = null
        _ringingAlarmId.value = null
        _ringingSince.value = 0L
        releaseWakeLock()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoSilenceTask)
        handler.removeCallbacks(reRaiseTask)
        player?.stop()
        player = null
        _ringingAlarmId.value = null
        _ringingSince.value = 0L
        releaseWakeLock()
        super.onDestroy()
    }

    // --- notification ---------------------------------------------------------

    private fun startForegroundCompat(alarm: Alarm) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        try {
            ServiceCompat.startForeground(this, AlarmNotifications.NOTIF_RING, buildNotification(alarm), type)
        } catch (t: Throwable) {
            Log.e(TAG, "startForeground failed", t)
        }
    }

    private fun notify(alarm: Alarm) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            nm.notify(AlarmNotifications.NOTIF_RING, buildNotification(alarm))
        } catch (t: Throwable) {
            Log.w(TAG, "Could not re-post the alarm notification", t)
        }
    }

    private fun buildNotification(alarm: Alarm): Notification {
        val fullScreen = PendingIntent.getActivity(
            this,
            9000 + alarm.id,
            ringIntent(this, alarm.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = alarm.label.ifBlank { "Alarm" } + "  " + alarm.timeText()
        val text = if (alarm.challenges.isEmpty()) {
            "Tap to stop the alarm"
        } else {
            alarm.challenges.joinToString(", ") { it.describe() }
        }

        val builder = NotificationCompat.Builder(this, AlarmNotifications.CHANNEL_RING)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSilent(true)
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)

        if (alarm.snoozeEnabled && AlarmStore.snoozesUsed(alarm.id) < alarm.maxSnoozes) {
            builder.addAction(
                0,
                "Snooze ${alarm.snoozeMinutes} min",
                servicePendingIntent(this, ACTION_SNOOZE, alarm.id, 7100)
            )
        }
        if (alarm.challenges.isEmpty()) {
            builder.addAction(
                0,
                "Stop",
                servicePendingIntent(this, ACTION_DISMISS, alarm.id, 7200)
            )
        }
        return builder.build()
    }

    private fun postMissedNotice(alarm: Alarm) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notice = NotificationCompat.Builder(this, AlarmNotifications.CHANNEL_INFO)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle("Missed alarm: ${alarm.timeText()}")
            .setContentText("It rang for ${alarm.autoSilenceMinutes} minutes and nobody solved it.")
            .setAutoCancel(true)
            .build()
        runCatching { nm.notify(AlarmNotifications.NOTIF_MISSED, notice) }
    }

    // --- wake lock ------------------------------------------------------------

    private fun acquireWakeLock(alarm: Alarm) {
        releaseWakeLock()
        val minutes = if (alarm.autoSilenceMinutes > 0) alarm.autoSilenceMinutes else 20
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jaago:ringing").apply {
            setReferenceCounted(false)
            runCatching { acquire(minutes * 60_000L + 60_000L) }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock -> runCatching { if (lock.isHeld) lock.release() } }
        wakeLock = null
    }

    companion object {
        private const val TAG = "JaagoService"
        private const val RE_RAISE_INTERVAL_MS = 15_000L

        const val ACTION_RING = "com.amrit.jaago.action.RING"
        const val ACTION_SNOOZE = "com.amrit.jaago.action.SNOOZE"
        const val ACTION_DISMISS = "com.amrit.jaago.action.DISMISS"
        const val EXTRA_ALARM_ID = "com.amrit.jaago.extra.ALARM_ID"

        private val _ringingAlarmId = MutableStateFlow<Int?>(null)

        /** The alarm that is making noise right now, if any. */
        val ringingAlarmId: StateFlow<Int?> = _ringingAlarmId.asStateFlow()

        private val _ringingSince = MutableStateFlow(0L)
        val ringingSince: StateFlow<Long> = _ringingSince.asStateFlow()

        fun ringIntent(context: Context, alarmId: Int): Intent =
            Intent(context, RingActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
                putExtra(EXTRA_ALARM_ID, alarmId)
            }

        private fun servicePendingIntent(
            context: Context,
            action: String,
            alarmId: Int,
            requestBase: Int
        ): PendingIntent {
            val intent = Intent(context, AlarmService::class.java).apply {
                this.action = action
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            return PendingIntent.getService(
                context,
                requestBase + alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun ring(context: Context, alarmId: Int) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_RING
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun dismiss(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply { action = ACTION_DISMISS }
            runCatching { context.startService(intent) }
        }

        fun snooze(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply { action = ACTION_SNOOZE }
            runCatching { context.startService(intent) }
        }
    }
}
